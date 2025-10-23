package dev.Felix.rifa_system.Exceptions;

public class ImagemException extends RuntimeException {
  public ImagemException(String message) {
    super(message);
  }
  public ImagemException(String message, Throwable cause) {
    super(message, cause);
  }

  public static ImagemException arquivoInvalido(String motivo) {
    return new ImagemException("Arquivo inválido: " + motivo);
  }

  public static ImagemException tamanhoExcedido(long tamanho, long maximo) {
    return new ImagemException(
            String.format("Arquivo muito grande: %.2fMB. Máximo: %.2fMB",
                    tamanho / 1024.0 / 1024.0,
                    maximo / 1024.0 / 1024.0)
    );
  }

  public static ImagemException tipoNaoSuportado(String tipo) {
    return new ImagemException("Tipo de arquivo não suportado: " + tipo);
  }
}
