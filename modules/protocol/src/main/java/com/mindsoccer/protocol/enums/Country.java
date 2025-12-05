package com.mindsoccer.protocol.enums;

/**
 * Pays supportés par le système.
 * Utilisé notamment pour le mode "Saut Patriotique" où les questions sont liées aux pays des joueurs.
 */
public enum Country {
    // Afrique de l'Ouest
    BEN("Bénin", "BEN", Language.FR),
    SEN("Sénégal", "SEN", Language.FR),
    CIV("Côte d'Ivoire", "CIV", Language.FR),
    MLI("Mali", "MLI", Language.FR),
    BFA("Burkina Faso", "BFA", Language.FR),
    TGO("Togo", "TGO", Language.FR),
    NER("Niger", "NER", Language.FR),
    GIN("Guinée", "GIN", Language.FR),
    GNB("Guinée-Bissau", "GNB", Language.PT),
    CPV("Cap-Vert", "CPV", Language.PT),
    GMB("Gambie", "GMB", Language.EN),
    GHA("Ghana", "GHA", Language.EN),
    NGA("Nigeria", "NGA", Language.EN),
    SLE("Sierra Leone", "SLE", Language.EN),
    LBR("Liberia", "LBR", Language.EN),

    // Afrique du Nord
    MAR("Maroc", "MAR", Language.AR),
    DZA("Algérie", "DZA", Language.AR),
    TUN("Tunisie", "TUN", Language.AR),
    LBY("Libye", "LBY", Language.AR),
    EGY("Égypte", "EGY", Language.AR),
    MRT("Mauritanie", "MRT", Language.AR),

    // Afrique Centrale
    CMR("Cameroun", "CMR", Language.FR),
    GAB("Gabon", "GAB", Language.FR),
    COG("Congo", "COG", Language.FR),
    COD("RD Congo", "COD", Language.FR),
    TCD("Tchad", "TCD", Language.FR),
    CAF("Centrafrique", "CAF", Language.FR),
    GNQ("Guinée équatoriale", "GNQ", Language.ES),

    // Afrique de l'Est
    ETH("Éthiopie", "ETH", Language.EN),
    KEN("Kenya", "KEN", Language.EN),
    TZA("Tanzanie", "TZA", Language.EN),
    UGA("Ouganda", "UGA", Language.EN),
    RWA("Rwanda", "RWA", Language.FR),
    BDI("Burundi", "BDI", Language.FR),
    DJI("Djibouti", "DJI", Language.FR),
    SOM("Somalie", "SOM", Language.AR),
    ERI("Érythrée", "ERI", Language.EN),
    SSD("Soudan du Sud", "SSD", Language.EN),
    SDN("Soudan", "SDN", Language.AR),

    // Afrique Australe
    ZAF("Afrique du Sud", "ZAF", Language.EN),
    MOZ("Mozambique", "MOZ", Language.PT),
    AGO("Angola", "AGO", Language.PT),
    ZMB("Zambie", "ZMB", Language.EN),
    ZWE("Zimbabwe", "ZWE", Language.EN),
    BWA("Botswana", "BWA", Language.EN),
    NAM("Namibie", "NAM", Language.EN),
    MWI("Malawi", "MWI", Language.EN),
    MDG("Madagascar", "MDG", Language.FR),
    MUS("Maurice", "MUS", Language.FR),
    SYC("Seychelles", "SYC", Language.FR),
    COM("Comores", "COM", Language.FR),

    // Europe
    FRA("France", "FRA", Language.FR),
    BEL("Belgique", "BEL", Language.FR),
    CHE("Suisse", "CHE", Language.FR),
    LUX("Luxembourg", "LUX", Language.FR),
    MCO("Monaco", "MCO", Language.FR),
    GBR("Royaume-Uni", "GBR", Language.EN),
    DEU("Allemagne", "DEU", Language.DE),
    ITA("Italie", "ITA", Language.IT),
    ESP("Espagne", "ESP", Language.ES),
    PRT("Portugal", "PRT", Language.PT),
    NLD("Pays-Bas", "NLD", Language.EN),
    POL("Pologne", "POL", Language.EN),
    ROU("Roumanie", "ROU", Language.FR),
    GRC("Grèce", "GRC", Language.EN),
    AUT("Autriche", "AUT", Language.DE),
    SWE("Suède", "SWE", Language.EN),
    NOR("Norvège", "NOR", Language.EN),
    DNK("Danemark", "DNK", Language.EN),
    FIN("Finlande", "FIN", Language.EN),
    IRL("Irlande", "IRL", Language.EN),

    // Amériques
    USA("États-Unis", "USA", Language.EN),
    CAN("Canada", "CAN", Language.EN),
    MEX("Mexique", "MEX", Language.ES),
    BRA("Brésil", "BRA", Language.PT),
    ARG("Argentine", "ARG", Language.ES),
    COL("Colombie", "COL", Language.ES),
    PER("Pérou", "PER", Language.ES),
    CHL("Chili", "CHL", Language.ES),
    HTI("Haïti", "HTI", Language.CR),
    DOM("République dominicaine", "DOM", Language.ES),
    CUB("Cuba", "CUB", Language.ES),
    JAM("Jamaïque", "JAM", Language.EN),
    TTO("Trinité-et-Tobago", "TTO", Language.EN),
    GUY("Guyana", "GUY", Language.EN),
    SUR("Suriname", "SUR", Language.EN),
    GUF("Guyane française", "GUF", Language.FR),
    MTQ("Martinique", "MTQ", Language.FR),
    GLP("Guadeloupe", "GLP", Language.FR),
    REU("La Réunion", "REU", Language.FR),
    MYT("Mayotte", "MYT", Language.FR),

    // Asie
    CHN("Chine", "CHN", Language.ZH),
    JPN("Japon", "JPN", Language.EN),
    KOR("Corée du Sud", "KOR", Language.EN),
    IND("Inde", "IND", Language.EN),
    THA("Thaïlande", "THA", Language.EN),
    VNM("Vietnam", "VNM", Language.EN),
    IDN("Indonésie", "IDN", Language.EN),
    MYS("Malaisie", "MYS", Language.EN),
    SGP("Singapour", "SGP", Language.EN),
    PHL("Philippines", "PHL", Language.EN),
    PAK("Pakistan", "PAK", Language.EN),
    BGD("Bangladesh", "BGD", Language.EN),
    LKA("Sri Lanka", "LKA", Language.EN),
    NPL("Népal", "NPL", Language.EN),

    // Moyen-Orient
    SAU("Arabie saoudite", "SAU", Language.AR),
    ARE("Émirats arabes unis", "ARE", Language.AR),
    QAT("Qatar", "QAT", Language.AR),
    KWT("Koweït", "KWT", Language.AR),
    OMN("Oman", "OMN", Language.AR),
    BHR("Bahreïn", "BHR", Language.AR),
    JOR("Jordanie", "JOR", Language.AR),
    LBN("Liban", "LBN", Language.AR),
    ISR("Israël", "ISR", Language.EN),
    TUR("Turquie", "TUR", Language.EN),
    IRN("Iran", "IRN", Language.EN),
    IRQ("Irak", "IRQ", Language.AR),

    // Océanie
    AUS("Australie", "AUS", Language.EN),
    NZL("Nouvelle-Zélande", "NZL", Language.EN),
    FJI("Fidji", "FJI", Language.EN),
    PNG("Papouasie-Nouvelle-Guinée", "PNG", Language.EN),
    NCL("Nouvelle-Calédonie", "NCL", Language.FR),
    PYF("Polynésie française", "PYF", Language.FR),
    WLF("Wallis-et-Futuna", "WLF", Language.FR);

    private final String displayName;
    private final String isoCode;
    private final Language defaultLanguage;

    Country(String displayName, String isoCode, Language defaultLanguage) {
        this.displayName = displayName;
        this.isoCode = isoCode;
        this.defaultLanguage = defaultLanguage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public Language getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Trouve un pays par son code ISO.
     */
    public static Country fromIsoCode(String isoCode) {
        for (Country country : values()) {
            if (country.isoCode.equalsIgnoreCase(isoCode)) {
                return country;
            }
        }
        return null;
    }

    /**
     * Retourne le pays par défaut pour une langue donnée.
     */
    public static Country getDefaultForLanguage(Language language) {
        return switch (language) {
            case FR -> FRA;
            case EN -> GBR;
            case PT -> PRT;
            case ES -> ESP;
            case AR -> MAR;
            case ZH -> CHN;
            case DE -> DEU;
            case IT -> ITA;
            case FON -> BEN;
            case CR -> HTI;
        };
    }
}
