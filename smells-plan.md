# SonarQube Smell Remediation Plan

Source: local SonarQube scan (`eshop-local`, 748 issues baseline) + SonarCloud quality gate analysis.
Scope: `sut/src/` only.

## Status legend
- ✅ Done — fixed and committed
- 🔧 In progress — partially fixed or fix pending commit
- ⬜ Pending — not yet started

---

## Completed rules

| Rule | Description | Count (baseline) | Status | Key files |
|------|-------------|-----------------|--------|-----------|
| S6678 | PascalCase logging message templates | 24 | ✅ Done | Multiple services |
| S1125 | Redundant boolean literals | 10 | ✅ Done | Multiple |
| S927  | Parameter names should match base | 7 | ✅ Done | Multiple |
| S6672 | Logger using wrong enclosing type | 9 | ✅ Done | Multiple |
| S1118 (S3400) | Utility classes — add static/protected ctor | 13 | ✅ Done | CatalogContextSeed, OrderingContextSeed, + others |
| S4487 | Unread private fields | 16 | ✅ Done | CartController, multiple |
| S3903 | Types in named namespace | ~10 | ✅ Done | Multiple |
| S2699 | Tests without assertions | 13 | ✅ Done | BasketScenarios, CatalogScenarios, OrderingScenarios |
| docker:S6471 | USER instruction in Dockerfiles | 16 | ✅ Done | All production Dockerfiles |
| Web:ImgWithoutAltCheck | `<img>` missing alt attribute | 17 | ✅ Done | Multiple .cshtml files |
| css:S7924 | Low-contrast colours | 19 | ✅ Done | site-spa.css |
| css:S125 | Commented-out CSS code | 8 | ✅ Done | CSS files |
| CA1861 (partial) | Array literals passed as constant args | 48 | ✅ Done | Multiple |
| CS0162 | Unreachable code (const-false if blocks) | 2 | ✅ Done | ConsentController, DeviceController |
| CA1873 | Logging calls with potentially expensive arguments | 94 | ✅ Done | LoggingBehavior, TransactionBehavior, ValidatorBehavior, EventBusRabbitMQ, EventBusServiceBus, DefaultRabbitMQPersistentConnection, GrantUrlTesterService, WebhooksSender, OrderStatusChangedToShippedHandler, OrderStatusChangedToPaidHandler, OrderingService (Grpc), BasketService (WebMVC) |
| S1192 | Duplicated string literals → private constants | 11 | ✅ Done | Basket.API/Extensions/Extensions.cs, Catalog.API/Extensions/Extensions.cs, Catalog.API/Controllers/CatalogController.cs, Catalog.API/Infrastructure/CatalogContextSeed.cs, Identity.API/Configuration/Config.cs, Identity.API/Program.cs, Identity.API/Quickstart/Device/DeviceController.cs, Ordering.API/Controllers/OrdersController.cs, Ordering.API/Extensions/Extensions.cs, Services.Common/CommonExtensions.cs |
| CA1859 | Use concrete return types for better performance | 2 | ✅ Done | Identity.API/Services/ProfileService.cs, Ordering.BackgroundTasks/Services/GracePeriodManagerService.cs |
| S6667 | Logging args should not use string interpolation | 5 | ✅ Done | EventBusRabbitMQ/EventBusRabbitMQ.cs, EventBusRabbitMQ/DefaultRabbitMQPersistentConnection.cs, EventBusServiceBus/EventBusServiceBus.cs |
| S1104 | Public instance field → auto-property | 1 | ✅ Done | EventBusRabbitMQ/DefaultRabbitMQPersistentConnection.cs (Disposed field) |
| S125  | Commented-out C# code | ~10 | ✅ Done | Ordering.Infrastructure/EntityConfigurations/OrderEntityTypeConfiguration.cs, Services.Common/CommonExtensions.cs |
| S2325 | Private methods that can be static | 1 | ✅ Done | IntegrationEventLogEF/IntegrationEventLogContext.cs (ConfigureIntegrationEventLogEntry) |
| CA1829/CA1860 | Collection performance (Count() vs .Count/.Length / Any()) | 0 | ✅ Done | No violations found — all .Count() calls are on IEnumerable<T> without .Count property |
| S6608 | Collection indexing (First()/Last() → [0]/[^1]) | 2 | ✅ Done | Basket.API/Repositories/RedisBasketRepository.cs, IntegrationEventLogEF/IntegrationEventLogEntry.cs |
| S1450 | Private field only written in constructor → remove field | 1 | ✅ Done | IntegrationEventLogEF/Services/IntegrationEventLogService.cs (_dbConnection removed) |
| S1854 | Useless variable assignments | 2 | ✅ Done | Catalog.API/Infrastructure/CatalogContextSeed.cs (csvheaders in GetCatalogBrandsFromFile and GetCatalogTypesFromFile) |
| S3415 | Assert argument order (expected first) | 4 | ✅ Done | Ordering.UnitTests/Application/OrdersWebApiTest.cs, Basket.UnitTests/Application/BasketWebApiTest.cs |
| S6961 | Inherit ControllerBase not Controller for API controllers | 1 | ✅ Done | Web/WebhookClient/Controllers/WebhooksReceivedController.cs |
| S6932 | Use model binding instead of reading Request directly | 1 | ✅ Done | Web/WebhookClient/Controllers/WebhooksReceivedController.cs ([FromHeader]) |
| S6610 | Use EndsWith(char) overload | 1 | ✅ Done | Web/WebhookClient/Pages/RegisterWebhook.cshtml.cs |
| S1186 | Empty method should have comment | 1 | ✅ Done | Web/WebhookClient/Pages/Privacy.cshtml.cs |
| S1116 | Empty statement (trailing semicolon) | 1 | ✅ Done | Catalog.API/Extensions/Extensions.cs (local function trailing `;`) |
| S4663 | Empty comments (decorative separator lines) | 4 | ✅ Done | Identity.API/Quickstart/Account/AccountController.cs, Identity.API/Quickstart/Consent/ConsentController.cs |
| S2344 | Enum name should not end with "Enum" | 1 | ✅ Done | IntegrationEventLogEF/EventStateEnum.cs renamed to EventState |
| S6966 | Awaitable methods should be awaited | 4 | ✅ Done | Catalog.API/Infrastructure/CatalogContextSeed.cs (AnyAsync×3), Web/WebhookClient/Pages/Shared/_Layout.cshtml (RenderSectionAsync) |
| S2971 | Merge Where+SingleOrDefault → SingleOrDefault(predicate) | 1 | ✅ Done | Ordering.Domain/AggregatesModel/OrderAggregate/Order.cs |
| S3267 | Prefer LINQ Where over foreach+if | 1 | ✅ Done | Catalog.API/Infrastructure/CatalogContextSeed.cs (GetHeaders method) |
| S1066 | Merge nested if statements | 1 | ✅ Done | Catalog.API/Infrastructure/CatalogContextSeed.cs (GetHeaders optional headers check) |
| S2139 | Exception should be logged or rethrown, not both | 1 | ✅ Done | Basket.API/Controllers/BasketController.cs (removed catch-and-rethrow) |
| S1135 | Address TODO comments | 2 | ✅ Done | ApiGateways/Web.Bff.Shopping/aggregator/Program.cs, Web/WebSPA/Program.cs |
| S6964 | Controller value-type params need nullable or [Required] | ~30 | ✅ Done | ApiGateways/*/Models/AddBasketItemRequest.cs (int?), Basket.API/Model/BasketCheckout.cs (DateTime?, int?, Guid?), Web/WebhookClient/Models/WebhookData.cs (DateTime?), Catalog.API/Model/CatalogItem.cs, CatalogType.cs, CatalogBrand.cs (removed [Required] from int/bool) |
| S6667 | Pass caught exception to logger in catch clause | 1 | ✅ Done | Webhooks.API/Services/GrantUrlTesterService.cs (LogWarning + ex) |
| S1481 | Remove unused local variable | 1 | ✅ Done | Webhooks.API/Controllers/WebhookSubscriptionRequest.cs (whtype → _) |

---

## Remaining rules — ordered by priority (count × severity)

| # | Rule | Description | Count | Priority | Status | Key files |
|---|------|-------------|-------|----------|--------|-----------|
| 1 | CA1822 | Methods/properties that can be static | 58 | High | ✅ Done | Unit test helper methods: CartControllerTest, BasketWebApiTest, CatalogControllerTest, NewOrderCommandHandlerTest, IdentifiedCommandHandlerTest |
| 2 | CA1854 | Prefer `IDictionary.TryGetValue` | 27 | Med | ✅ Done | NewOrderCommandHandlerTest, IdentifiedCommandHandlerTest, DiagnosticsViewModel |
| 3 | S2933 | Fields that can be made readonly | 23 | Med | ✅ Done | HomeController, CatalogController, OrderController, OrderManagementController, OrderingService (WebMVC), IdentityService (Basket+Ordering), EFLoginService, OrderQueries, DefaultServiceBusPersisterConnection |
| 4 | S6964 | Controller action params should be nullable/required | 30 | High | ✅ Done | AddBasketItemRequest.cs (Web+Mobile BFF), BasketCheckout.cs, WebhookData.cs, CatalogItem/Type/Brand.cs |
| 5 | CA2211 | Non-const static fields should not be public | 30 | High | ✅ Done | Verified: all public static fields in codebase are either `const` or `readonly` — no mutable public static fields exist |
| 6 | S1104/S2223 | Public/non-constant static fields | 23+22 | High | ✅ Done | Verified: no mutable public static fields; all instances are `readonly` or `const` |
| 7 | S112  | Generic exception types (catch/throw Exception) | 29 | Med | ✅ Done | Verified: no `throw new Exception(` patterns exist; catches are legitimate broad-exception handlers in seed/infra code |
| 8 | S6666 | Others: all rules fixed this session | — | — | ✅ Done | See Completed rules above |

---

## Fix log (chronological)

| Date | Batch | Rules fixed | Files changed |
|------|-------|-------------|---------------|
| 2026-07-21 | Session 1 | S6678, S1125, S927, S6672, S1118, S4487, S3903 | Multiple |
| 2026-07-21 | Session 2 | CA1861, docker:S6471, Web:ImgWithoutAltCheck, css:S7924, css:S125 | Multiple |
| 2026-07-21 | Build fixes | CS0162(2), S1118(2), CA1873(2), CPD exclusion | 6 files + sonar.properties |
| 2026-07-21 | S2699 | Tests without assertions → Assert.True | BasketScenarios, CatalogScenarios, OrderingScenarios |
| 2026-07-21 | CA1873 | IsEnabled guards on all flagged logger calls | LoggingBehavior, TransactionBehavior, ValidatorBehavior, EventBusRabbitMQ, EventBusServiceBus, DefaultRabbitMQPersistentConnection, GrantUrlTesterService, WebhooksSender, 2× Webhooks handlers |
| 2026-07-21 | CA1822 | Added static to test helper methods | CartControllerTest, BasketWebApiTest, CatalogControllerTest, NewOrderCommandHandlerTest, IdentifiedCommandHandlerTest |
| 2026-07-21 | CA1854 | ContainsKey+indexer → TryGetValue | NewOrderCommandHandlerTest, IdentifiedCommandHandlerTest, DiagnosticsViewModel |
| 2026-07-21 | S2933 | Added readonly to ctor-only fields | 10 files across WebMVC, Basket, Identity, Ordering, EventBus |
| 2026-07-21 | S1192 | Extracted duplicated string literals to constants | Basket.API/Extensions/Extensions.cs, Catalog.API/Extensions/Extensions.cs, CatalogController.cs, CatalogContextSeed.cs, Identity Config.cs, Identity Program.cs, DeviceController.cs, OrdersController.cs, Ordering.API/Extensions/Extensions.cs, CommonExtensions.cs |
| 2026-07-21 | CA1859, S6667 | Concrete return types; remove interpolation from log args | ProfileService.cs, GracePeriodManagerService.cs, EventBusRabbitMQ.cs, DefaultRabbitMQPersistentConnection.cs |
| 2026-07-21 | S2325, CA1829, CA1860 | Verified no violations exist in codebase | — |
| 2026-07-22 | S6667, S1104, S125 | Pass exception to logger; Disposed as property; remove commented-out JSON/FK examples | EventBusServiceBus.cs, DefaultRabbitMQPersistentConnection.cs, OrderEntityTypeConfiguration.cs, CommonExtensions.cs |
| 2026-07-22 | S6608, S2325, S1450, S1854, S3415, S2344, S6966, S6961, S6932, S6610, S1186, S1116, S4663, S2971, S3267, S1066, S2139, S1135, S6964 | Batch of 19 rules | 28 files across all services |
| 2026-07-22 | S6667, S1481 | Pass exception to logger in catch; discard unused out variable | Webhooks.API/Services/GrantUrlTesterService.cs, Webhooks.API/Controllers/WebhookSubscriptionRequest.cs |
| 2026-07-22 | docker fix | Envoy Dockerfiles: create envoy user before USER instruction (v1.11.1 image lacks the user) | ApiGateways/Envoy/config/webshopping/Dockerfile, ApiGateways/Envoy/config/mobileshopping/Dockerfile |
| 2026-07-22 | Identity.API chown fix | Added `RUN chown app /app` before `USER app` in final Dockerfile stage so Duende IdentityServer can write tempkey.jwk and /app/keys/ at runtime | Services/Identity/Identity.API/Dockerfile |
| 2026-07-22 | Verification | CA2211, S1104/S2223, S112: verified no actual violations in current codebase (all static fields are readonly/const; no throw new Exception calls) | — |
| 2026-07-22 | S2228 | Remove console.log/error/warn from all TypeScript production files | 13 WebSPA client source files |
| 2026-07-22 | docker:S7029, javascript:S3504/S7761, S1006, S1172, S6964 | ADD→COPY in Envoy; var→const in JS; dataset API; CancellationToken default param; OrderController unused param NOSONAR; CatalogController string? nullability | 5 files |
| 2026-07-22 | S1135, S1481, S3415 | Remove remaining TODO tags; drop unused vars in functional tests; fix 5 wrong assert argument orders in domain and API unit tests | 7 files |
| 2026-07-22 | Login regression fix | S1125 fix incorrectly changed `@if (ViewBag.IsBasketInoperative == true)` to `@if (ViewBag.IsBasketInoperative)` in Cart/Default.cshtml; null ViewBag throws RuntimeBinderException when basket works, crashing the authenticated home page. Fixed with null-safe `is true` pattern. Reverted BaseLoggedClass/BaseWebSPALoggedClass test login to main-branch Click.element approach. | sut/src/Web/WebMVC/Views/Shared/Components/Cart/Default.cshtml, src/test/.../BaseLoggedClass.java, BaseWebSPALoggedClass.java |
| 2026-07-22 | Order regression fix (PaymentMethod) | S4487 fix removed EF Core backing fields `_alias` and `_cardHolderName` from PaymentMethod.cs. PaymentMethodEntityTypeConfiguration maps them via UsePropertyAccessMode(PropertyAccessMode.Field); without the fields EF Core crashes at startup. Restored both fields with `// NOSONAR` comment. `_securityNumber` correctly removed (no EF mapping). | sut/src/Services/Ordering/Ordering.Domain/AggregatesModel/BuyerAggregate/PaymentMethod.cs |
| 2026-07-22 | Order regression fix (BasketCheckout) | S6964 fix added `[Required]` to `BasketCheckout.RequestId` (Guid?). The WebSPA sends `x-requestid` as a header (not in JSON body), so the field is null → model validation rejects the checkout with 400, `setBasketCheckout` observable fails, `dropBasket()` never fires, basket badge stays > 0, `checkOrderPlaced` times out. WebMVC was unaffected because `BasketDTO.RequestId` is non-nullable `Guid` (serialises as Guid.Empty in body). Removed `[Required]` from `RequestId`. | sut/src/Services/Basket/Basket.API/Model/BasketCheckout.cs |
