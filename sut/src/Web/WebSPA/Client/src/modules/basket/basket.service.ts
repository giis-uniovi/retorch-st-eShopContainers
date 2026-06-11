import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { DataService } from '../shared/services/data.service';
import { SecurityService } from '../shared/services/security.service';
import { IBasket } from '../shared/models/basket.model';
import { IOrder } from '../shared/models/order.model';
import { IBasketCheckout } from '../shared/models/basketCheckout.model';
import { BasketWrapperService } from '../shared/services/basket.wrapper.service';
import { ConfigurationService } from '../shared/services/configuration.service';
import { StorageService } from '../shared/services/storage.service';

import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class BasketService {
    private basketUrl: string = '';
    private purchaseUrl: string = '';
    basket: IBasket = {
        buyerId: '',
        items: []
    };

    private readonly basketUpdateSource = new Subject<void>();
    readonly basketUpdate$ = this.basketUpdateSource.asObservable();

    constructor(
        private readonly service: DataService,
        private readonly authService: SecurityService,
        private readonly basketWrapperService: BasketWrapperService,
        private readonly router: Router,
        private readonly configurationService: ConfigurationService,
        private readonly storageService: StorageService
    ) {
        this.basket.items = [];

        if (this.authService.IsAuthorized) {
            if (this.authService.UserData) {
                this.basket.buyerId = this.authService.UserData.sub;
                if (this.configurationService.isReady) {
                    this.basketUrl = this.configurationService.serverSettings.purchaseUrl;
                    this.purchaseUrl = this.configurationService.serverSettings.purchaseUrl;
                    this.loadData();
                } else {
                    this.configurationService.settingsLoaded$.subscribe(x => {
                        this.basketUrl = this.configurationService.serverSettings.purchaseUrl;
                        this.purchaseUrl = this.configurationService.serverSettings.purchaseUrl;
                        this.loadData();
                    });
                }
            }
        }

        this.basketWrapperService.orderCreated$.subscribe(x => {
            this.dropBasket();
        });
    }

    addItemToBasket(item): Observable<boolean> {
        const basketItem = this.basket.items.find(value => value.productId == item.productId);

        if (basketItem) {
            basketItem.quantity++;
        } else {
            this.basket.items.push(item);
        }

        return this.setBasket(this.basket);
    }

    setBasket(basket): Observable<boolean> {
        const url = this.purchaseUrl + '/b/api/v1/basket/';

        this.basket = basket;

        return this.service.post(url, basket).pipe<boolean>(tap((response: any) => true));
    }

    setBasketCheckout(basketCheckout): Observable<boolean> {
        const url = this.basketUrl + '/b/api/v1/basket/checkout';

        return this.service.postWithId(url, basketCheckout).pipe<boolean>(tap((response: any) => {
            this.basketWrapperService.orderCreated();
            return true;
        }));
    }

    getBasket(): Observable<IBasket> {
        const url = this.basketUrl + '/b/api/v1/basket/' + this.basket.buyerId;

        return this.service.get(url).pipe<IBasket>(tap((response: any) => {
            if (response.status === 204) {
                return null;
            }

            return response;
        }));
    }

    mapBasketInfoCheckout(order: IOrder): IBasketCheckout {
        const basketCheckout = <IBasketCheckout>{};

        basketCheckout.street = order.street;
        basketCheckout.city = order.city;
        basketCheckout.country = order.country;
        basketCheckout.state = order.state;
        basketCheckout.zipcode = order.zipcode;
        basketCheckout.cardexpiration = order.cardexpiration;
        basketCheckout.cardnumber = order.cardnumber;
        basketCheckout.cardsecuritynumber = order.cardsecuritynumber;
        basketCheckout.cardtypeid = order.cardtypeid;
        basketCheckout.cardholdername = order.cardholdername;
        basketCheckout.total = 0;
        basketCheckout.expiration = order.expiration;

        return basketCheckout;
    }

    updateQuantity() {
        this.basketUpdateSource.next();
    }

    dropBasket() {
        this.basket.items = [];
        this.setBasket(this.basket).subscribe(res => {
            this.basketUpdateSource.next();
        });
    }

    private loadData() {
        this.getBasket().subscribe(basket => {
            if (basket != null)
                this.basket.items = basket.items;
        });
    }
}
