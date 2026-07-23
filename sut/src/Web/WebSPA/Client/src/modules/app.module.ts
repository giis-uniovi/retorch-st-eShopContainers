import { NgModule } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async'; // NOSONAR typescript:S1874
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient } from "@angular/common/http";

import { routing } from './app.routes';
import { AppService } from './app.service';
import { AppComponent } from './app.component';
import { SharedModule } from './shared/shared.module';
import { CatalogModule } from './catalog/catalog.module';
import { OrdersModule } from './orders/orders.module';
import { BasketModule } from './basket/basket.module';
import { ToastrModule } from 'ngx-toastr';

@NgModule({
    declarations: [AppComponent],
    imports: [
        BrowserModule,
        ToastrModule.forRoot(),
        routing,
        // Only module that app module loads
        SharedModule.forRoot(),
        CatalogModule,
        OrdersModule,
        BasketModule
    ],
    providers: [
        AppService,
        provideAnimationsAsync(), // NOSONAR typescript:S1874
        provideHttpClient()
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
