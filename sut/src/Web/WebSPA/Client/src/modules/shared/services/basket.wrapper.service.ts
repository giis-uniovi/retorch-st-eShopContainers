import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

import { ICatalogItem } from '../models/catalogItem.model';
import { IBasketItem } from '../models/basketItem.model';
import { IBasket } from '../models/basket.model';
import { SecurityService } from '../services/security.service';
import { Guid } from '../../../guid';

@Injectable()
export class BasketWrapperService {
    public basket: IBasket;

    constructor(private readonly identityService: SecurityService) { }

    private readonly addItemToBasketSource = new Subject<IBasketItem>();
    readonly addItemToBasket$ = this.addItemToBasketSource.asObservable();

    private readonly orderCreatedSource = new Subject<void>();
    readonly orderCreated$ = this.orderCreatedSource.asObservable();

    addItemToBasket(item: ICatalogItem) {
        if (this.identityService.IsAuthorized) {
            const basketItem: IBasketItem = {
                pictureUrl: item.pictureUri,
                productId: item.id,
                productName: item.name,
                quantity: 1,
                unitPrice: item.price,
                id: Guid.newGuid(),
                oldUnitPrice: 0
            };

            this.addItemToBasketSource.next(basketItem);
        } else {
            this.identityService.Authorize();
        }
    }

    orderCreated() {
        this.orderCreatedSource.next();
    }
}
