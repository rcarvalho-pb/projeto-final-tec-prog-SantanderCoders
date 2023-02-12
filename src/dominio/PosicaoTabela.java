package dominio;

import java.util.Objects;

public record PosicaoTabela(Time time,
                            Long vitorias,
                            Long derrotas,
                            Long empates,
                            Long golsPositivos,
                            Long golsSofridos,
                            Long saldoDeGols,
                            Long jogos) {

    public Long pontos(){
        return vitorias*3+empates;
    }

    @Override
    public String toString() {
        return  time +
                ", pontos=" + pontos() + // desenvolver forma
            // de obter a
            // pontuação
                ", vitorias=" + vitorias +
                ", derrotas=" + derrotas +
                ", empates=" + empates +
                ", golsPositivos=" + golsPositivos +
                ", golsSofridos=" + golsSofridos +
                ", saldoDeGols=" + saldoDeGols +
                ", jogos=" + jogos +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PosicaoTabela that = (PosicaoTabela) o;
        return Objects.equals(vitorias, that.vitorias) && Objects.equals(golsPositivos, that.golsPositivos) && Objects.equals(saldoDeGols, that.saldoDeGols);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vitorias, golsPositivos, saldoDeGols);
    }
}
