namespace Microsoft.eShopOnContainers.Services.Identity.API.Data;

public class ApplicationDbContext : IdentityDbContext<ApplicationUser> // NOSONAR S110
{
    public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
        : base(options)
    {
    }


}
