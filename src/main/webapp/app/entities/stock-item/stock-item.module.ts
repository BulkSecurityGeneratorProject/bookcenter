import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { BookCenterSharedModule } from '../../shared';
import {
    StockItemService,
    StockItemPopupService,
    StockItemComponent,
    StockItemDetailComponent,
    StockItemDialogComponent,
    StockItemPopupComponent,
    StockItemDeletePopupComponent,
    StockItemDeleteDialogComponent,
    stockItemRoute,
    stockItemPopupRoute,
    StockItemResolvePagingParams,
} from './';

const ENTITY_STATES = [
    ...stockItemRoute,
    ...stockItemPopupRoute,
];

@NgModule({
    imports: [
        BookCenterSharedModule,
        RouterModule.forChild(ENTITY_STATES)
    ],
    declarations: [
        StockItemComponent,
        StockItemDetailComponent,
        StockItemDialogComponent,
        StockItemDeleteDialogComponent,
        StockItemPopupComponent,
        StockItemDeletePopupComponent,
    ],
    entryComponents: [
        StockItemComponent,
        StockItemDialogComponent,
        StockItemPopupComponent,
        StockItemDeleteDialogComponent,
        StockItemDeletePopupComponent,
    ],
    providers: [
        StockItemService,
        StockItemPopupService,
        StockItemResolvePagingParams,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class BookCenterStockItemModule {}
