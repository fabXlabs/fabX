import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QualificationDetailsComponent } from './qualification-details.component';

describe('QualificationDetailsComponent', () => {
    let component: QualificationDetailsComponent;
    let fixture: ComponentFixture<QualificationDetailsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [QualificationDetailsComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(QualificationDetailsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
