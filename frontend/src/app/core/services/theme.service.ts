import { Injectable, signal } from '@angular/core';

const CHAVE_TEMA = 'controle-estoque:tema';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly escuro = signal<boolean>(this.carregarPreferencia());

  constructor() {
    this.aplicar(this.escuro());
  }

  alternar(): void {
    this.definir(!this.escuro());
  }

  definir(escuro: boolean): void {
    this.escuro.set(escuro);
    this.aplicar(escuro);
    try {
      localStorage.setItem(CHAVE_TEMA, escuro ? 'dark' : 'light');
    } catch {
      // localStorage indisponivel (ex: modo privado) - preferencia nao sera persistida
    }
  }

  private aplicar(escuro: boolean): void {
    document.documentElement.classList.toggle('dark-theme', escuro);
  }

  private carregarPreferencia(): boolean {
    try {
      const salvo = localStorage.getItem(CHAVE_TEMA);
      if (salvo) {
        return salvo === 'dark';
      }
    } catch {
      // ignora e cai no fallback do sistema
    }
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;
  }
}
