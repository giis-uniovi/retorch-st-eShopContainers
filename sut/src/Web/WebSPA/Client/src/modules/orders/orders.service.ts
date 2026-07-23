import { Injectable } from '@angular/core';

import { DataService } from '../shared/services/data.service';
import { IOrder } from '../shared/models/order.model';
import { IOrderItem } from '../shared/models/orderItem.model';
import { IOrderDetail } from "../shared/models/order-detail.model";
import { SecurityService } from '../shared/services/security.service';
import { ConfigurationService } from '../shared/services/configuration.service';
import { BasketWrapperService } from '../shared/services/basket.wrapper.service';

import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable()
export class OrdersService {
    private ordersUrl: string = '';

    constructor(
        private readonly service: DataService,
        private readonly basketService: BasketWrapperService,
        private readonly identityService: SecurityService,
        private readonly configurationService: ConfigurationService
    ) {
        if (this.configurationService.isReady)
            this.ordersUrl = this.configurationService.serverSettings.purchaseUrl;
        else
            this.configurationService.settingsLoaded$.subscribe(() => this.ordersUrl = this.configurationService.serverSettings.purchaseUrl);
    }

    getOrders(): Observable<IOrder[]> {
        const url = this.ordersUrl + '/o/api/v1/orders';

        return this.service.get(url).pipe<IOrder[]>(tap((response: any) => {
            return response;
        }));
    }

    cancelOrder(orderNumber: number): Observable<any> {
        const url = this.ordersUrl + '/o/api/v1/orders/cancel';
        const data = { OrderNumber: orderNumber };

        return this.service.putWithId(url, data).pipe<any>(tap(() => { }));
    }

    getOrder(id: number): Observable<IOrderDetail> {
        const url = this.ordersUrl + '/o/api/v1/orders/' + id;

        return this.service.get(url).pipe<IOrderDetail>(tap((response: any) => {
            return response;
        }));
    }

    mapOrderAndIdentityInfoNewOrder(): IOrder {
        const order = <IOrder>{};
        const basket = this.basketService.basket;
        const identityInfo = this.identityService.UserData;

        order.street = identityInfo.address_street;
        order.city = identityInfo.address_city;
        order.country = identityInfo.address_country;
        order.state = identityInfo.address_state;
        order.zipcode = identityInfo.address_zip_code;
        order.cardexpiration = identityInfo.card_expiration;
        order.cardnumber = identityInfo.card_number;
        order.cardsecuritynumber = identityInfo.card_security_number;
        order.cardtypeid = identityInfo.card_type;
        order.cardholdername = identityInfo.card_holder;
        order.total = 0;
        order.expiration = identityInfo.card_expiration;

        order.orderItems = new Array<IOrderItem>();
        basket.items.forEach(x => {
            const item: IOrderItem = <IOrderItem>{};
            item.pictureurl = x.pictureUrl;
            item.productId = +x.productId;
            item.productname = x.productName;
            item.unitprice = x.unitPrice;
            item.units = x.quantity;

            order.total += (item.unitprice * item.units);

            order.orderItems.push(item);
        });

        order.buyer = basket.buyerId;

        return order;
    }
}
