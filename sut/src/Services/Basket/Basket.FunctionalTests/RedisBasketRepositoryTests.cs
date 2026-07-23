using Basket.API.Repositories;

namespace Basket.FunctionalTests
{
    public class RedisBasketRepositoryTests
        : BasketScenarioBase
    {
        private const string FakeBuyerId = "buyerId";

        [Fact]
        public async Task UpdateBasket_return_and_add_basket()
        {
            var server = CreateServer();
            var redis = server.Services.GetRequiredService<ConnectionMultiplexer>();

            var redisBasketRepository = BuildBasketRepository(redis);

            var basket = await redisBasketRepository.UpdateBasketAsync(new CustomerBasket("customerId")
            {
                BuyerId = FakeBuyerId,
                Items = BuildBasketItems()
            });

            Assert.NotNull(basket);
            Assert.Single(basket.Items);
        }

        [Fact]
        public async Task Delete_Basket_return_null()
        {
            var server = CreateServer();
            var redis = server.Services.GetRequiredService<ConnectionMultiplexer>();

            var redisBasketRepository = BuildBasketRepository(redis);

            var basket = await redisBasketRepository.UpdateBasketAsync(new CustomerBasket("customerId")
            {
                BuyerId = FakeBuyerId,
                Items = BuildBasketItems()
            });

            var deleteResult = await redisBasketRepository.DeleteBasketAsync(FakeBuyerId);

            var result = await redisBasketRepository.GetBasketAsync(basket.BuyerId);

            Assert.True(deleteResult);
            Assert.Null(result);
        }

        static RedisBasketRepository BuildBasketRepository(ConnectionMultiplexer connMux)
        {
            var loggerFactory = new LoggerFactory();
            return new RedisBasketRepository(loggerFactory.CreateLogger<RedisBasketRepository>(), connMux);
        }

        static List<BasketItem> BuildBasketItems()
        {
            return new List<BasketItem>()
            {
                new BasketItem()
                {
                    Id = "basketId",
                    PictureUrl = "pictureurl",
                    ProductId = 1,
                    ProductName = "productName",
                    Quantity = 1,
                    UnitPrice = 1
                }
            };
        }
    }
}
