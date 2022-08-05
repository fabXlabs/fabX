import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QualificationAddComponent } from './qualification-add.component';

describe('QualificationAddComponent', () => {
    let component: QualificationAddComponent;
    let fixture: ComponentFixture<QualificationAddComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [QualificationAddComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(QualificationAddComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
