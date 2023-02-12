package impl;

import dominio.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CampeonatoBrasileiroImpl {

    private Map<Integer, List<Jogo>> brasileirao;
    private List<Jogo> jogos;
    private Predicate<Jogo> filtro;
    public CampeonatoBrasileiroImpl(Path arquivo, Predicate<Jogo> filtro) throws IOException {
        this.jogos = lerArquivo(arquivo);
        this.filtro = filtro;
        this.brasileirao = jogos.stream()
                .filter(filtro) //filtrar por ano
                .collect(Collectors.groupingBy(
                        Jogo::rodada,
                        Collectors.mapping(Function.identity(), Collectors.toList())));


    }

    public List<Jogo> lerArquivo(Path file) throws IOException {
        DateTimeFormatter dtfData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dtfHora = DateTimeFormatter.ofPattern("HH:mm");
        return Files.lines(file).skip(1).map(string -> {
                    String[] dados = string.split(";");
                    Integer rodada = Integer.parseInt(dados[0]);
                    LocalDate data =
                        LocalDate.parse(dados[1].isEmpty() ? "22/07/1994" :
                                dados[1],
                            dtfData);
                    LocalTime hora =
                        LocalTime.parse(dados[2].isEmpty() ? "00:00" :
                            dados[2].replace(
                        "h",
                        ":"), dtfHora);
                    DayOfWeek dia = data.getDayOfWeek();
                    DataDoJogo dataDoJogo = new DataDoJogo(data, hora, dia);
                    Time mandante = new Time(dados[4]);
                    Time visitante = new Time(dados[5]);
                    Time vencedor = new Time(dados[6]);
                    String arena = dados[7];
                    Integer mandantePlacar =
                        Integer.parseInt(dados[8].replace("[- ]+", "0"));
                    Integer visitantePlacar =
                        Integer.parseInt(dados[9].replace("[- ]+", "0"));
                    String estadoMandante = dados[10];
                    String estadoVisitante = dados[11];
                    String estadoVencedor = dados[12];

                    return new Jogo(rodada, dataDoJogo, mandante, visitante,
                        vencedor, arena, mandantePlacar, visitantePlacar,
                        estadoMandante, estadoVisitante, estadoVencedor);

        }).toList();

    }

    public IntSummaryStatistics getEstatisticasPorJogo() {
        return jogos.stream()
            .filter(filtro)
            .map(jogo -> jogo.visitantePlacar() + jogo.mandantePlacar())
            .collect(IntSummaryStatistics::new,
                IntSummaryStatistics::accept,
                IntSummaryStatistics::combine);
    }

    public Long getTotalVitoriasEmCasa() {
        return getTodosOsPlacares()
            .entrySet()
            .stream().
            filter(jogo -> jogo.getKey().mandante() > jogo.getKey().visitante())
            .map(Map.Entry::getValue)
            .reduce(0L, Long::sum);
    }

    public Long getTotalVitoriasForaDeCasa() {
        return getTodosOsPlacares()
            .entrySet()
            .stream().
            filter(jogo -> jogo.getKey().mandante() < jogo.getKey().visitante())
            .map(Map.Entry::getValue)
            .reduce(0L, Long::sum);
    }

    public Long getTotalEmpates() {
        return getTodosOsPlacares()
            .entrySet()
            .stream().
            filter(jogo -> jogo.getKey().mandante() == jogo.getKey().visitante())
            .map(Map.Entry::getValue)
            .reduce(0L, Long::sum);
    }

    public Long getTotalJogosComMenosDe3Gols() {
        return getTodosOsPlacares()
            .entrySet()
            .stream().
            filter(jogo -> jogo.getKey().mandante() + jogo.getKey().visitante() < 3)
            .map(Map.Entry::getValue)
            .reduce(0L, Long::sum);
    }

    public Long getTotalJogosCom3OuMaisGols() {
        return getTodosOsPlacares()
            .entrySet()
            .stream().
            filter(jogo -> jogo.getKey().mandante() + jogo.getKey().visitante() >= 3)
            .map(Map.Entry::getValue)
            .reduce(0L, Long::sum);

    }

    public Map<Resultado, Long> getTodosOsPlacares() {
        return jogos.stream()
            .filter(filtro)
            .map(jogo -> {
                return new Resultado(jogo.mandantePlacar(),
                    jogo.visitantePlacar());})
            .collect(Collectors.groupingBy(jogo -> new Resultado(jogo.mandante(), jogo.visitante()),
                Collectors.counting()
            ));
    }

    public Map.Entry<Resultado, Long> getPlacarMaisRepetido() {
       return getTodosOsPlacares().entrySet().stream().max((a, b) -> a.getValue().compareTo(b.getValue())).get();

    }

    public Map.Entry<Resultado, Long> getPlacarMenosRepetido() {
        return getTodosOsPlacares().entrySet().stream().min((a, b) -> a.getValue().compareTo(b.getValue())).get();
    }

    public Set<PosicaoTabela> getTabela() {
      Set<PosicaoTabela> posicaoTabelaMandante = new HashSet<>();
      Set<PosicaoTabela> posicaoTabelaVisitante = new HashSet<>();

      Map<Time, List<Jogo>> timeMandante = jogos.stream()
          .filter(filtro) //filtrar por ano
          .collect(Collectors.groupingBy(
              Jogo::mandante,
              Collectors.mapping(Function.identity(), Collectors.toList())));
      Map<Time, List<Jogo>> timeVisitante = jogos.stream()
          .filter(filtro) //filtrar por ano
          .collect(Collectors.groupingBy(
              Jogo::visitante,
              Collectors.mapping(Function.identity(), Collectors.toList())));

      Map<Time, List<Jogo>> timeVencedor = jogos.stream()
          .filter(filtro) //filtrar por ano
          .collect(Collectors.groupingBy(
              Jogo::visitante,
              Collectors.mapping(Function.identity(), Collectors.toList())));

      


      for (Map.Entry<Time, List<Jogo>> clube : timeMandante.entrySet()) {
        Long vitorias = clube.getValue().stream()
            .filter(jogo -> jogo.mandantePlacar() > jogo.visitantePlacar())
            .count();
        Long derrotas = clube.getValue().stream()
            .filter(jogo -> jogo.mandantePlacar() < jogo.visitantePlacar())
            .count();
        Long empates = clube.getValue().stream()
            .filter(jogo -> jogo.mandantePlacar() == jogo.visitantePlacar())
            .count();
        Long golsFeitos = clube.getValue().stream()
            .map(Jogo::mandantePlacar)
            .reduce(0, Integer::sum).longValue();
        Long golsSofridos = clube.getValue().stream()
            .map(Jogo::visitantePlacar)
            .reduce(0, Integer::sum).longValue();
        Long saldoDeGols = golsFeitos - golsSofridos;
        Long jogos = clube.getValue().stream().count();

        posicaoTabelaMandante.add(new PosicaoTabela(clube.getKey(), vitorias,
            derrotas, empates, golsFeitos, golsSofridos, saldoDeGols,
            jogos));
      }

      for (Map.Entry<Time, List<Jogo>> clube : timeVisitante.entrySet()) {
        Long vitorias = clube.getValue().stream()
            .filter(jogo -> jogo.mandantePlacar() < jogo.visitantePlacar())
            .count();
        Long derrotas = clube.getValue().stream()
            .filter(jogo -> jogo.mandantePlacar() > jogo.visitantePlacar())
            .count();
        Long empates = clube.getValue().stream()
            .filter(jogo -> jogo.mandantePlacar() == jogo.visitantePlacar())
            .count();
        Long golsFeitos = clube.getValue().stream()
            .map(Jogo::visitantePlacar)
            .reduce(0, Integer::sum).longValue();
        Long golsSofridos = clube.getValue().stream()
            .map(Jogo::mandantePlacar)
            .reduce(0, Integer::sum).longValue();
        Long saldoDeGols = golsFeitos - golsSofridos;
        Long jogos = clube.getValue().stream().count();

        posicaoTabelaVisitante.add(new PosicaoTabela(clube.getKey(), vitorias,
            derrotas, empates, golsFeitos, golsSofridos, saldoDeGols,
            jogos));
      }
      Set<PosicaoTabela> saida = new HashSet<>();


//      for (PosicaoTabela k : posicaoTabelaMandante) {
//        saida.add(k);
//      }
//      for (PosicaoTabela k : posicaoTabelaVisitante) {
//        System.out.println(saida.contains(k));
//      }
      saida.addAll(posicaoTabelaMandante);
      saida.addAll(posicaoTabelaVisitante);
      return saida;
    }

}