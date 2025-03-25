package si.aris.randomizer2.service;
import org.springframework.stereotype.Service;
import si.aris.randomizer2.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PregledOpredelitevRecenzentov {

    public static class RecenzentEntry {
        public int sifraRecenzenta;
        public String status; // DA, NE, Neopredeljen, Dodeljena
        public boolean primarno;

        public RecenzentEntry(int sifra, String status, boolean primarno) {
            this.sifraRecenzenta = sifra;
            this.status = status;
            this.primarno = primarno;
        }
    }

    public static class PrijavaEntry {
        public int prijavaId;
        public boolean interdisc;
        public List<RecenzentEntry> recenzenti;

        public PrijavaEntry(int prijavaId, boolean interdisc, List<RecenzentEntry> recenzenti) {
            this.prijavaId = prijavaId;
            this.interdisc = interdisc;
            this.recenzenti = recenzenti;
        }
    }

    /**
     * Ugotovi ali je recenzent primaren ali ne za podano prijavo.
     */
    public static Optional<Boolean> dolociPrimarnost(Prijava prijava, Recenzent recenzent) {
        boolean imaPrimarnoPodpodrocje = recenzent.getRecenzentiPodrocja().stream()
                .anyMatch(rp -> rp.getPodpodrocjeId() == prijava.getPodpodrocje().getPodpodrocjeId());

        boolean imaPrimarniErc = recenzent.getRecenzentiErc().stream()
                .anyMatch(re -> re.getErcPodrocjeId() == prijava.getErcPodrocje().getErcId());

        boolean jePrimarno = imaPrimarnoPodpodrocje && imaPrimarniErc;

        if (jePrimarno) return Optional.of(true);

        if (prijava.getDodatnoPodpodrocje() != null && prijava.getDodatnoErcPodrocje() != null) {
            boolean imaDodatnoPodpodrocje = recenzent.getRecenzentiPodrocja().stream()
                    .anyMatch(rp -> rp.getPodpodrocjeId() == prijava.getDodatnoPodpodrocje().getPodpodrocjeId());

            boolean imaDodatniErc = recenzent.getRecenzentiErc().stream()
                    .anyMatch(re -> re.getErcPodrocjeId() == prijava.getDodatnoErcPodrocje().getErcId());

            boolean jeDodatno = imaDodatnoPodpodrocje && imaDodatniErc;

            if (jeDodatno) return Optional.of(false);
        }

        return Optional.empty(); // Ne pripada tej prijavi
    }

    /**
     * Glavna funkcija, ki izračuna najbolj pogoste pare za vsako prijavo.
     */
    public Map<Integer, Set<Integer>> dodeliPrijave(List<PrijavaEntry> prijave) {
        Map<Integer, Set<Integer>> dodelitve = new HashMap<>();

        for (PrijavaEntry prijava : prijave) {
            List<RecenzentEntry> primarni = prijava.recenzenti.stream()
                    .filter(r -> r.primarno && r.status.equalsIgnoreCase("DA"))
                    .toList();

            List<RecenzentEntry> dodatni = prijava.recenzenti.stream()
                    .filter(r -> !r.primarno && r.status.equalsIgnoreCase("DA"))
                    .toList();

            List<Set<Integer>> pari = new ArrayList<>();

            if (prijava.interdisc) {
                for (RecenzentEntry p : primarni) {
                    for (RecenzentEntry d : dodatni) {
                        pari.add(Set.of(p.sifraRecenzenta, d.sifraRecenzenta));
                    }
                }
            } else {
                List<RecenzentEntry> daRecenzenti = prijava.recenzenti.stream()
                        .filter(r -> r.status.equalsIgnoreCase("DA"))
                        .toList();

                for (int i = 0; i < daRecenzenti.size(); i++) {
                    for (int j = i + 1; j < daRecenzenti.size(); j++) {
                        pari.add(Set.of(
                                daRecenzenti.get(i).sifraRecenzenta,
                                daRecenzenti.get(j).sifraRecenzenta
                        ));
                    }
                }
            }

            // Najpogostejši par
            Map<Set<Integer>, Long> pogostost = pari.stream()
                    .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

            Optional<Map.Entry<Set<Integer>, Long>> najpogostejsi = pogostost.entrySet().stream()
                    .max(Map.Entry.comparingByValue());

            najpogostejsi.ifPresent(pair -> dodelitve.put(prijava.prijavaId, pair.getKey()));
        }

        return dodelitve;
    }
}
