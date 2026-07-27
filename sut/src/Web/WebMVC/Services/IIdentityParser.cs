namespace Microsoft.eShopOnContainers.WebMVC.Services;

public interface IIdentityParser<out T>
{
    T Parse(IPrincipal principal);
}
