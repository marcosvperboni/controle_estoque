import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { finalize } from 'rxjs';
import { Produto, ProdutoRequest } from '../../../core/models/produto.model';
import { ProdutoService } from '../../../core/services/produto.service';

export interface FormularioProdutoDialogData {
  produto: Produto | null;
}

@Component({
  selector: 'app-formulario-produto-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  templateUrl: './formulario-produto-dialog.component.html',
  styleUrl: './formulario-produto-dialog.component.scss'
})
export class FormularioProdutoDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly produtoService = inject(ProdutoService);
  private readonly dialogRef = inject(MatDialogRef<FormularioProdutoDialogComponent>);
  protected readonly data = inject<FormularioProdutoDialogData>(MAT_DIALOG_DATA);

  protected salvando = false;
  protected readonly modoEdicao = !!this.data.produto;

  protected readonly form = this.fb.nonNullable.group({
    nome: [this.data.produto?.nome ?? '', [Validators.required, Validators.maxLength(150)]],
    codigoBarras: [
      this.data.produto?.codigoBarras ?? '',
      [Validators.required, Validators.maxLength(50)]
    ],
    fornecedor: [
      this.data.produto?.fornecedor ?? '',
      [Validators.required, Validators.maxLength(150)]
    ],
    marca: [this.data.produto?.marca ?? '', [Validators.required, Validators.maxLength(100)]],
    dataValidade: [this.data.produto?.dataValidade ? new Date(this.data.produto.dataValidade) : null],
    precoVarejo: [this.data.produto?.precoVarejo ?? 0, [Validators.required, Validators.min(0.01)]],
    precoAtacado: [this.data.produto?.precoAtacado ?? 0, [Validators.required, Validators.min(0.01)]],
    precoCompra: [this.data.produto?.precoCompra ?? 0, [Validators.required, Validators.min(0.01)]],
    precoVenda: [this.data.produto?.precoVenda ?? 0, [Validators.required, Validators.min(0.01)]],
    quantidade: [this.data.produto?.quantidade ?? 0, [Validators.required, Validators.min(0)]],
    quantidadeMinima: [this.data.produto?.quantidadeMinima ?? 0, [Validators.required, Validators.min(0)]]
  });

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const request: ProdutoRequest = {
      ...valores,
      dataValidade: valores.dataValidade
        ? new Date(valores.dataValidade).toISOString().substring(0, 10)
        : null
    };

    this.salvando = true;
    const requisicao = this.modoEdicao
      ? this.produtoService.atualizar(this.data.produto!.id, request)
      : this.produtoService.cadastrar(request);

    requisicao.pipe(finalize(() => (this.salvando = false))).subscribe({
      next: (produto) => this.dialogRef.close(produto)
    });
  }

  protected cancelar(): void {
    this.dialogRef.close();
  }
}
