// Copyright (c) Brock Allen & Dominick Baier. All rights reserved.
// Licensed under the Apache License, Version 2.0. See LICENSE in the project root for license information.

namespace IdentityServerHost.Quickstart.UI;

public static class AccountOptions
{
    public const bool AllowLocalLogin = true;
    public const bool AllowRememberLogin = true;
    public static readonly TimeSpan RememberMeLoginDuration = TimeSpan.FromDays(30);

    public const bool ShowLogoutPrompt = false;
    public const bool AutomaticRedirectAfterSignOut = true;

    public const string InvalidCredentialsErrorMessage = "Invalid username or password";
}
