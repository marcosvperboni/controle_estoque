import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

interface ApiError {
  mensagem?: string;
  detalhes?: string[];
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const apiError = error.error as ApiError | undefined;
      const mensagem = apiError?.detalhes?.length
        ? apiError.detalhes.join(' | ')
        : (apiError?.mensagem ?? 'Ocorreu um erro inesperado ao comunicar com o servidor');

      snackBar.open(mensagem, 'Fechar', { duration: 5000, panelClass: 'snackbar-erro' });
      return throwError(() => error);
    })
  );
};
