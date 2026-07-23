namespace Microsoft.eShopOnContainers.Web.Shopping.HttpAggregator.Services;

public class BasketService : IBasketService
{
    private readonly Basket.BasketClient _basketClient;
    private readonly ILogger<BasketService> _logger;

    public BasketService(Basket.BasketClient basketClient, ILogger<BasketService> logger)
    {
        _basketClient = basketClient;
        _logger = logger;
    }

    public async Task<BasketData> GetByIdAsync(string id)
    {
        if (_logger.IsEnabled(LogLevel.Debug))
            _logger.LogDebug("grpc client created, request = {@Id}", id);
        var response = await _basketClient.GetBasketByIdAsync(new BasketRequest { Id = id });
        if (_logger.IsEnabled(LogLevel.Debug))
            _logger.LogDebug("grpc response {@Response}", response);

        return MapToBasketData(response);
    }

    public async Task UpdateAsync(BasketData currentBasket)
    {
        if (_logger.IsEnabled(LogLevel.Debug))
            _logger.LogDebug("Grpc update basket currentBasket {@CurrentBasket}", currentBasket);
        var request = MapToCustomerBasketRequest(currentBasket);
        if (_logger.IsEnabled(LogLevel.Debug))
            _logger.LogDebug("Grpc update basket request {@Request}", request);

        await _basketClient.UpdateBasketAsync(request);
    }

    private static BasketData MapToBasketData(CustomerBasketResponse customerBasketRequest)
    {
        if (customerBasketRequest == null)
        {
            return null;
        }

        var map = new BasketData
        {
            BuyerId = customerBasketRequest.Buyerid
        };

        customerBasketRequest.Items.ToList().ForEach(item =>
        {
            if (item.Id != null)
            {
                map.Items.Add(new BasketDataItem
                {
                    Id = item.Id,
                    OldUnitPrice = (decimal)item.Oldunitprice,
                    PictureUrl = item.Pictureurl,
                    ProductId = item.Productid,
                    ProductName = item.Productname,
                    Quantity = item.Quantity,
                    UnitPrice = (decimal)item.Unitprice
                });
            }
        });

        return map;
    }

    private static CustomerBasketRequest MapToCustomerBasketRequest(BasketData basketData)
    {
        if (basketData == null)
        {
            return null;
        }

        var map = new CustomerBasketRequest
        {
            Buyerid = basketData.BuyerId
        };

        basketData.Items.ToList().ForEach(item =>
        {
            if (item.Id != null)
            {
                map.Items.Add(new BasketItemResponse
                {
                    Id = item.Id,
                    Oldunitprice = (double)item.OldUnitPrice,
                    Pictureurl = item.PictureUrl,
                    Productid = item.ProductId,
                    Productname = item.ProductName,
                    Quantity = item.Quantity,
                    Unitprice = (double)item.UnitPrice
                });
            }
        });

        return map;
    }
}
