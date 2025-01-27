package si.aris.randomizer2.model;

import java.util.HashMap;
import java.util.Map;

public class PartnerskeAgencije {

    public static final Map<String, String> PARTNERSKE_AGENCIJE_MAP = new HashMap<>();

    static {
        PARTNERSKE_AGENCIJE_MAP.put("GAČR", "Češka");
        PARTNERSKE_AGENCIJE_MAP.put("FWF", "Avstrija");
        PARTNERSKE_AGENCIJE_MAP.put("HRZZ", "Hrvaška");
        PARTNERSKE_AGENCIJE_MAP.put("NKFIH", "Madžarska");
        PARTNERSKE_AGENCIJE_MAP.put("NCN", "Poljska");
        PARTNERSKE_AGENCIJE_MAP.put("FWO", "Belgija");
        PARTNERSKE_AGENCIJE_MAP.put("FNR", "Luksemburg");
        PARTNERSKE_AGENCIJE_MAP.put("SNSF", "Švica");
        // Dodaj druge partnerske agencije, če so potrebne
    }
}
