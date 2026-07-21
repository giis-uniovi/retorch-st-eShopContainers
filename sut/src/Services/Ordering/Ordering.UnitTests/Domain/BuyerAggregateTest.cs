namespace Ordering.UnitTests.Domain;

public class BuyerAggregateTest
{
    private const string FakeAlias = "fakeAlias";
    private const string FakeCardNumber = "124";
    private const string FakeSecurityNumber = "1234";
    private const string FakeCardHolderName = "FakeHolderNAme";
    private const string FakeUser = "fakeUser";

    public BuyerAggregateTest()
    { }

    [Fact]
    public void Create_buyer_item_success()
    {
        //Arrange    
        var identity = new Guid().ToString();
        var name = FakeUser;

        //Act 
        var fakeBuyerItem = new Buyer(identity, name);

        //Assert
        Assert.NotNull(fakeBuyerItem);
    }

    [Fact]
    public void Create_buyer_item_fail()
    {
        //Arrange    
        var identity = string.Empty;
        var name = FakeUser;

        //Act - Assert
        Assert.Throws<ArgumentNullException>(() => new Buyer(identity, name));
    }

    [Fact]
    public void add_payment_success()
    {
        //Arrange    
        var cardTypeId = 1;
        var alias = FakeAlias;
        var cardNumber = FakeCardNumber;
        var securityNumber = FakeSecurityNumber;
        var cardHolderName = FakeCardHolderName;
        var expiration = DateTime.Now.AddYears(1);
        var orderId = 1;
        var name = FakeUser;
        var identity = new Guid().ToString();
        var fakeBuyerItem = new Buyer(identity, name);

        //Act
        var result = fakeBuyerItem.VerifyOrAddPaymentMethod(cardTypeId, alias, cardNumber, securityNumber, cardHolderName, expiration, orderId);

        //Assert
        Assert.NotNull(result);
    }

    [Fact]
    public void create_payment_method_success()
    {
        //Arrange    
        var cardTypeId = 1;
        var alias = FakeAlias;
        var cardNumber = FakeCardNumber;
        var securityNumber = FakeSecurityNumber;
        var cardHolderName = FakeCardHolderName;
        var expiration = DateTime.Now.AddYears(1);
        var fakePaymentMethod = new PaymentMethod(cardTypeId, alias, cardNumber, securityNumber, cardHolderName, expiration);

        //Act
        var result = new PaymentMethod(cardTypeId, alias, cardNumber, securityNumber, cardHolderName, expiration);

        //Assert
        Assert.NotNull(result);
    }

    [Fact]
    public void create_payment_method_expiration_fail()
    {
        //Arrange    
        var cardTypeId = 1;
        var alias = FakeAlias;
        var cardNumber = FakeCardNumber;
        var securityNumber = FakeSecurityNumber;
        var cardHolderName = FakeCardHolderName;
        var expiration = DateTime.Now.AddYears(-1);

        //Act - Assert
        Assert.Throws<OrderingDomainException>(() => new PaymentMethod(cardTypeId, alias, cardNumber, securityNumber, cardHolderName, expiration));
    }

    [Fact]
    public void payment_method_isEqualTo()
    {
        //Arrange    
        var cardTypeId = 1;
        var alias = FakeAlias;
        var cardNumber = FakeCardNumber;
        var securityNumber = FakeSecurityNumber;
        var cardHolderName = FakeCardHolderName;
        var expiration = DateTime.Now.AddYears(1);

        //Act
        var fakePaymentMethod = new PaymentMethod(cardTypeId, alias, cardNumber, securityNumber, cardHolderName, expiration);
        var result = fakePaymentMethod.IsEqualTo(cardTypeId, cardNumber, expiration);

        //Assert
        Assert.True(result);
    }

    [Fact]
    public void Add_new_PaymentMethod_raises_new_event()
    {
        //Arrange    
        var alias = FakeAlias;
        var orderId = 1;
        var cardTypeId = 5;
        var cardNumber = "12";
        var cardSecurityNumber = "123";
        var cardHolderName = "FakeName";
        var cardExpiration = DateTime.Now.AddYears(1);
        var expectedResult = 1;
        var name = FakeUser;

        //Act 
        var fakeBuyer = new Buyer(Guid.NewGuid().ToString(), name);
        fakeBuyer.VerifyOrAddPaymentMethod(cardTypeId, alias, cardNumber, cardSecurityNumber, cardHolderName, cardExpiration, orderId);

        //Assert
        Assert.Equal(expectedResult, fakeBuyer.DomainEvents.Count);
    }
}