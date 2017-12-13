import { TestBed, inject } from '@angular/core/testing';

import { GlobalService } from './global.service';

describe('GlobalsService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GlobalService]
    });
  });

  it('should be created', inject([GlobalService], (service: GlobalService) => {
    expect(service).toBeTruthy();
  }));
});
