import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('dark-theme');
    TestBed.configureTestingModule({});
  });

  it('deve alternar entre tema claro e escuro e refletir na classe do html', () => {
    const service = TestBed.inject(ThemeService);
    const estadoInicial = service.escuro();

    service.alternar();

    expect(service.escuro()).toBe(!estadoInicial);
    expect(document.documentElement.classList.contains('dark-theme')).toBe(!estadoInicial);
  });

  it('deve persistir a preferencia de tema no localStorage', () => {
    const service = TestBed.inject(ThemeService);

    service.definir(true);

    expect(localStorage.getItem('controle-estoque:tema')).toBe('dark');
    expect(document.documentElement.classList.contains('dark-theme')).toBe(true);
  });
});
