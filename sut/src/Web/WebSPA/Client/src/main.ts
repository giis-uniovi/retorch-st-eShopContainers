import { enableProdMode, importProvidersFrom } from '@angular/core';
import { bootstrapApplication, BrowserModule } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { ToastrModule } from 'ngx-toastr';

import { AppComponent } from './modules/app.component';
import { AppService } from './modules/app.service';
import { routing } from './modules/app.routes';
import { SharedModule } from './modules/shared/shared.module';
import { CatalogModule } from './modules/catalog/catalog.module';
import { OrdersModule } from './modules/orders/orders.module';
import { BasketModule } from './modules/basket/basket.module';
import { environment } from './environments/environment';

if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(
      BrowserModule,
      ToastrModule.forRoot(),
      routing,
      SharedModule.forRoot(),
      CatalogModule,
      OrdersModule,
      BasketModule
    ),
    AppService,
    provideHttpClient()
  ]
});
