import { NgModule }                     from '@angular/core';

import { SharedModule }                 from '../shared/shared.module';
import { BasketComponent }              from './basket.component';
import { BasketStatusComponent }        from './basket-status/basket-status.component';

@NgModule({
    imports: [SharedModule],
    declarations: [BasketComponent, BasketStatusComponent],
    exports: [BasketStatusComponent]
})
export class BasketModule { }
