# Project Structure Guide: User Registration (Hexagonal + Decorator)

## 1. High-Level Overview

Dependencies always flow **inward**. The Domain is a pure Java library. The Infrastructure layer "decorates" the Domain to make it compatible with Spring Boot's dependency injection and transaction management.

---

## 2. Directory Hierarchy

```texts
com.example.user
├── domain
│   ├── common             # Core utilities (Result, CorrelationId)
│   ├── exception          # Pure Java Exceptions (DomainException, UserNotFoundException)
│   ├── model
│   │   ├── entity         # User.java
│   │   └── valueobject    # Email.java, Password.java
│   └── service            # Pure Java Logic (UserRegistrationService.java)
├── application
│   └── port
│       ├── in             # RegisterUserUseCase.java (Interface)
│       └── out            # UserRepositoryPort.java, MessageBusPort.java
└── infrastructure
    ├── adapter
    │   ├── in
    │   │   ├── rest       # Controllers & GlobalProblemHandler.java
    │   │   └── decorator  # Spring @Service wrappers (SpringRegistrationDecorator.java)
    │   └── out
    │       ├── persistence # JpaUserRepositoryAdapter.java
    │       └── kafka       # KafkaMessageBusAdapter.java
    ├── config             # Serialization, Security, Kafka Config
    └── UserApplication.java # Spring Boot Launcher

```

---

## 3. Core Implementation (The "Pure" Part)

### **Domain Exception**

```java
// domain.exception
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}

```

### **Domain Service (POJO)**

Notice there are **no annotations**. This class can be tested in milliseconds without a Spring context.

```java
// domain.service
public class UserRegistrationService { // Internal logic only
    private final UserRepositoryPort repository;

    public UserRegistrationService(UserRepositoryPort repository) {
        this.repository = repository;
    }

    public User execute(User user) {
        if (repository.existsByEmail(user.getEmail())) {
            throw new DomainException("Email already registered");
        }
        return repository.save(user);
    }
}

```

---

## 4. The Decorator (The Spring Part)

Instead of a `BeanConfig` file, we create a decorator in the infrastructure layer. This class is responsible for **Transaction Management** and **Spring Service Registration**.

```java
// infrastructure.adapter.in.decorator
@Service // Spring picks this up
public class SpringRegistrationDecorator implements RegisterUserUseCase {
    
    private final UserRegistrationService domainService;
    private final UserRepositoryPort repository;

    public SpringRegistrationDecorator(UserRepositoryPort repository) {
        // We manually instantiate the domain service here
        this.domainService = new UserRegistrationService(repository);
        this.repository = repository;
    }

    @Override
    @Transactional // Database transaction starts here
    public User register(User user) {
        return domainService.execute(user);
    }
}

```

---

## 5. Global Error Handling

The `RestControllerAdvice` captures the pure `DomainException` and converts it to a standard RFC 7807 `ProblemDetail`.

```java
// infrastructure.adapter.in.rest
@RestControllerAdvice
public class GlobalProblemHandler {

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralError(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }
}

```

---

## 6. Summary for the Template Repository

| Component | Layer | Purpose | Framework Allowed? |
| --- | --- | --- | --- |
| **Common/Models** | `domain` | Business state & common types | **No** |
| **Exceptions** | `domain` | Custom business error types | **No** |
| **Ports** | `application` | Interfaces for DI | **No** |
| **Service Logic** | `domain` | The "What" (Business Logic) | **No** |
| **Decorators** | `infrastructure` | The "How" (Spring, @Transactional) | **Yes** |
| **Adapters** | `infrastructure` | External integrations (DB, Kafka) | **Yes** |

### Why use this Decorator approach?

1. **Purity:** Your domain code remains 100% independent.
2. **Explicit Intent:** The `@Transactional` annotation is exactly where it belongs—at the entry point of a use case.
3. **Productivity:** You don't have to maintain a massive `BeanConfig` file; you just create a new decorator for every Use Case.
