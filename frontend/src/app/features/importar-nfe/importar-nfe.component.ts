import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { NotaFiscalImportResponse } from '../../core/models/produto.model';
import { NotaFiscalService } from '../../core/services/nota-fiscal.service';

@Component({
  selector: 'app-importar-nfe',
  imports: [MatButtonModule, MatCardModule, MatChipsModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './importar-nfe.component.html',
  styleUrl: './importar-nfe.component.scss'
})
export class ImportarNfeComponent {
  private readonly notaFiscalService = inject(NotaFiscalService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly enviando = signal(false);
  protected readonly resultado = signal<NotaFiscalImportResponse | null>(null);
  protected arquivoSelecionado: File | null = null;

  protected selecionarArquivo(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.arquivoSelecionado = input.files?.item(0) ?? null;
    this.resultado.set(null);
  }

  protected importar(): void {
    if (!this.arquivoSelecionado) {
      return;
    }

    this.enviando.set(true);
    this.notaFiscalService
      .importar(this.arquivoSelecionado)
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe((resposta) => {
        this.resultado.set(resposta);
        this.arquivoSelecionado = null;
        this.snackBar.open('NF-e importada com sucesso', 'Fechar', { duration: 3000 });
      });
  }
}
