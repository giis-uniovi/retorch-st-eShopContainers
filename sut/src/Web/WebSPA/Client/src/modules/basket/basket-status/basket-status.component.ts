import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Subscription }         from 'rxjs';

import { BasketService }        from '../basket.service';
import { BasketWrapperService } from '../../shared/services/basket.wrapper.service';
import { SecurityService }      from '../../shared/services/security.service';
import { ConfigurationService }      from '../../shared/services/configuration.service';

@Component({
  standalone: false,
    selector: 'esh-basket-status',
    styleUrls: ['./basket-status.component.scss'],
    templateUrl: './basket-status.component.html'
})
export class BasketStatusComponent implements OnInit {
    basketItemAddedSubscription: Subscription;
    basketUpdateSubscription: Subscription;
    authSubscription: Subscription;

    badge: number = 0;

    constructor(
        private basketService: BasketService,
        private basketWrapperService: BasketWrapperService,
        private authService: SecurityService,
        private configurationService: ConfigurationService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        // Subscribe to Add Basket Observable:
        this.basketItemAddedSubscription = this.basketWrapperService.addItemToBasket$.subscribe(item => {
            this.basketService.addItemToBasket(item).subscribe(res => {
                this.basketService.getBasket().subscribe(basket => {
                    if (basket)
                        this.badge = basket.items.length;
                    this.cdr.detectChanges();
                });
            });
        });

        this.basketUpdateSubscription = this.basketService.basketUpdate$.subscribe(res => {
            this.basketService.getBasket().subscribe(basket => {
                this.badge = basket ? basket.items.length : 0;
                this.cdr.detectChanges();
            });
        });

        // Subscribe to login and logout observable
        this.authSubscription = this.authService.authenticationChallenge$.subscribe(res => {
            this.basketService.getBasket().subscribe(basket => {
                if (basket != null)
                    this.badge = basket.items.length;
                this.cdr.detectChanges();
            });
        });

        // Init:
        if (this.configurationService.isReady) {
            this.basketService.getBasket().subscribe(basket => {
                if (basket != null)
                    this.badge = basket.items.length;
                this.cdr.detectChanges();
            });
        } else {
            this.configurationService.settingsLoaded$.subscribe(x => {
                this.basketService.getBasket().subscribe(basket => {
                    if (basket != null)
                        this.badge = basket.items.length;
                    this.cdr.detectChanges();
                });
            });
        }
    }
}

