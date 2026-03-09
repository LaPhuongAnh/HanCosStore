# Copilot instructions for DemoDATN2

## Project overview
- Spring Boot MVC app (Java 21) with Thymeleaf templates under [src/main/resources/templates](src/main/resources/templates) and static assets under [src/main/resources/static](src/main/resources/static).
- Controllers return template names (see [src/main/java/com/example/demodatn2/controller](src/main/java/com/example/demodatn2/controller)); admin UI templates live in [src/main/resources/templates/admin](src/main/resources/templates/admin).

## Key flows and architecture
- Session auth: `AuthService` stores `LOGIN_USER` and `ROLES` in session, and `AuthInterceptor` enforces role-based access + redirects/401s for `/api/**` (see [src/main/java/com/example/demodatn2/service/AuthService.java](src/main/java/com/example/demodatn2/service/AuthService.java) and [src/main/java/com/example/demodatn2/interceptor/AuthInterceptor.java](src/main/java/com/example/demodatn2/interceptor/AuthInterceptor.java)).
- Cart flow: `CartService` persists guest carts by `sessionId`; `CartController` keeps `CART_COUNT` plus voucher session keys `APPLIED_VOUCHER_CODE` and `DISCOUNT_AMOUNT` (see [src/main/java/com/example/demodatn2/service/CartService.java](src/main/java/com/example/demodatn2/service/CartService.java) and [src/main/java/com/example/demodatn2/controller/CartController.java](src/main/java/com/example/demodatn2/controller/CartController.java)).
- Product flow: `SanPhamService` creates product + variants + images; `SanPhamRepository` uses `@EntityGraph` to avoid `MultipleBagFetchException` and `HomeServiceimpl` composes view models (see [src/main/java/com/example/demodatn2/service/SanPhamService.java](src/main/java/com/example/demodatn2/service/SanPhamService.java), [src/main/java/com/example/demodatn2/repository/SanPhamRepository.java](src/main/java/com/example/demodatn2/repository/SanPhamRepository.java), [src/main/java/com/example/demodatn2/service/impl/HomeServiceimpl.java](src/main/java/com/example/demodatn2/service/impl/HomeServiceimpl.java)).
- File uploads: `FileUploadController` stores product images in `src/main/resources/static/images/products/` and `WebConfig` exposes `/images/products/**` from disk (see [src/main/java/com/example/demodatn2/controller/FileUploadController.java](src/main/java/com/example/demodatn2/controller/FileUploadController.java) and [src/main/java/com/example/demodatn2/config/WebConfig.java](src/main/java/com/example/demodatn2/config/WebConfig.java)).
- Scheduled jobs: `OrderStatusScheduler` auto-completes delivered orders nightly (see [src/main/java/com/example/demodatn2/service/OrderStatusScheduler.java](src/main/java/com/example/demodatn2/service/OrderStatusScheduler.java)).

## Data, seeds, and integrations
- SQL Server connection and app settings live in [src/main/resources/application.properties](src/main/resources/application.properties) (`ddl-auto=none` means schema is managed outside the app).
- `DataInitializer` seeds roles (ADMIN/STAFF/CUSTOMER) and demo users (admin/nhanvien/user) with password `123456` (see [src/main/java/com/example/demodatn2/config/DataInitializer.java](src/main/java/com/example/demodatn2/config/DataInitializer.java)).
- Password reset uses Spring Mail + tokens, base URL from `app.reset-password.base-url` (see [src/main/java/com/example/demodatn2/service/ResetPasswordService.java](src/main/java/com/example/demodatn2/service/ResetPasswordService.java)).
- Gemini chatbot calls Google’s REST API via `GeminiClient` and requires `GEMINI_API_KEY` (see [src/main/java/com/example/demodatn2/service/GeminiClient.java](src/main/java/com/example/demodatn2/service/GeminiClient.java)).

## Local dev workflows
- Run app: `mvnw.cmd spring-boot:run` (Windows) or `./mvnw spring-boot:run` (macOS/Linux). Main class is `DemoDatn2Application` at [src/main/java/com/example/demodatn2/DemoDatn2Application.java](src/main/java/com/example/demodatn2/DemoDatn2Application.java).
- Tests: `mvnw.cmd test` (if needed).

## Project-specific conventions
- Keep role checks aligned with `AuthInterceptor` URL rules when adding new routes.
- Update session `CART_COUNT` after cart mutations to keep header badge consistent.
- For product detail pages, use repository methods with `@EntityGraph` instead of eager-loading multiple collections in a single query.
