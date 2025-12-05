-- Flyway Migration V1: Seed questions from "Mindsoccer Zinsou" book
-- Source: MINDSOCCER - La Bible de la Culture Tome 12 by AZEDCET

-- =====================================================
-- SMASH A / SMASH B Questions (questions vérifiables)
-- Ces questions peuvent être utilisées par les équipes
-- =====================================================

INSERT INTO ms_question (id, text_fr, answer, round_type, difficulty, question_format, active, usage_count, success_count, source, created_at, updated_at)
VALUES
-- Questions SMASH issues du match exemple
(gen_random_uuid(), 'Quel volcan est le point culminant de l''équateur ?', 'COTOPAXI', 'SMASH_A', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel volcan de l''Antarctique fut gravi par les membres de l''expédition de SHACKLETON ?', 'EREBUS', 'SMASH_A', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel volcan, dont le nom veut dire « Montagne blanche » est sur l''île HAWAÏ ?', 'MAUNA KEA', 'SMASH_A', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel volcan dévasta le 8 mai 1902, la ville de Saint-Pierre ?', 'PELE', 'SMASH_A', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'A qui devons nous au BENIN, cette phrase restée célèbre : « Monseigneur, la conférence est souveraine » ?', 'Mathieu KEREKOU', 'SMASH_A', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel acteur béninois a joué dans « Gladiator » ?', 'DJIMON HOUNSOU', 'SMASH_A', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel titre du roi d''Abomey fait de lui le roi de la terre ?', 'AÏNON', 'SMASH_A', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Qui a écrit : « Pauvreté et gestion des peuples » ?', 'Albert TEVOEDJRE', 'SMASH_A', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Dans quel pays d''Afrique du nord se trouve « La porte de France » ?', 'TUNISIE', 'SMASH_B', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel président africain vint au pouvoir en 2000, après avoir concouru cinq fois de suite ?', 'ABDOULAYE WADE', 'SMASH_B', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel arbre d''Afrique surnomme-t-on « Arbre bouteille » ?', 'BAOBAB', 'SMASH_B', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- DUEL Questions (langage, expressions latines, etc.)
-- =====================================================

INSERT INTO ms_question (id, text_fr, answer, round_type, difficulty, question_format, active, usage_count, success_count, source, created_at, updated_at)
VALUES
(gen_random_uuid(), 'Que veut dire l''expression latine « In saecula saeculorum » ?', 'Dans les siècles des siècles', 'DUEL', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Dites en latin « Allez ! La messe est dite ! »', 'Ite Missa est', 'DUEL', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Que veut dire l''expression « Fama Volat » ?', 'La renommée s''envole', 'DUEL', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Dites en latin « Que la lumière soit ! »', 'FIAT LUX', 'DUEL', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Que veut dire l''expression latine Vox Populi, Vox Dei ?', 'Voix du peuple, voix de Dieu', 'DUEL', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- ESTOCADE Questions (40 points chacune, difficiles)
-- =====================================================

INSERT INTO ms_question (id, text_fr, answer, round_type, difficulty, points, question_format, active, usage_count, success_count, source, created_at, updated_at)
VALUES
(gen_random_uuid(), 'Né près du lac Baïkal en 1167, grand dirigeant ?', 'Gengis KHAN', 'ESTOCADE', 'HARD', 40, 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Substance dérivée de glucides complexes, analogue à l''amidon et produit par de nombreux végétaux ?', 'PECTINE', 'ESTOCADE', 'HARD', 40, 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Né le 4 mars 1810 à ZELAZOWA-WOLA, artiste ?', 'FREDERIC CHOPIN', 'ESTOCADE', 'HARD', 40, 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- MARATHON Questions (10 questions d'endurance)
-- =====================================================

INSERT INTO ms_question (id, text_fr, answer, round_type, difficulty, question_format, active, usage_count, success_count, source, created_at, updated_at)
VALUES
(gen_random_uuid(), 'Quel nom fut donné à la ville russe de Perm de 1940 à 1957 ?', 'MOLOTOV', 'MARATHON', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel pape, 236-255, fut victime de la persécution de Decius ?', 'SAINT-FABIEN', 'MARATHON', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel homme politique est né à Abéokuta en 1937 ?', 'OLUSEGUN OBASANJO', 'MARATHON', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel amiral japonais, commandant la flotte japonaise, dirigea l''attaque sur Pearl Harbor ?', 'YAMAMOTO ISOROKU', 'MARATHON', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel nom donne-t-on à un endroit où on a beaucoup fumé, qui conserve l''odeur du tabac ?', 'TABAGIE', 'MARATHON', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel nom donne-t-on au chef de province dans l''empire ottoman ?', 'PACHA', 'MARATHON', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle ville est ZARAGOZA en espagnol ?', 'SARAGOSSE', 'MARATHON', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel nom porte le personnage des livres pour enfants, roi des éléphants au costume vert ?', 'BABAR', 'MARATHON', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- SPRINT_FINAL Questions (20 éclairs rapides)
-- =====================================================

INSERT INTO ms_question (id, text_fr, answer, round_type, difficulty, question_format, active, usage_count, success_count, source, created_at, updated_at)
VALUES
(gen_random_uuid(), 'Quel aéronaute français réalisa le 1er vol en montgolfière en 1783 ?', 'Pilâtre de ROSIER', 'SPRINT_FINAL', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel nom porte ce grand chasseur, roi de Babel, fondateur de Ninive ?', 'NEMROD', 'SPRINT_FINAL', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel nom porte la reine légendaire d''Assyrie fondatrice de Babylone ?', 'SEMIRAMIS', 'SPRINT_FINAL', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Comment surnomme-t-on Abebe BIKILA ?', 'Athlète aux pieds nus', 'SPRINT_FINAL', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Que vous rappelle la date du 21 Mars 1960 en AFRIQUE DU SUD ?', 'MASSACRE DE SHARPEVILLE', 'SPRINT_FINAL', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel empereur haïtien est connu sous le nom de Jacques 1er ?', 'Jean-Jacques DESSALINES', 'SPRINT_FINAL', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Que décerne-t-on au Festival de Rio de Janeiro ?', 'TOUCANS D''or', 'SPRINT_FINAL', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Au cours de quelle bataille le célèbre Davy CROCKETT trouva-t-il la mort ?', 'FORT ALAMO', 'SPRINT_FINAL', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Qui a écrit « Le dernier jour d''un condamné » ?', 'Victor HUGO', 'SPRINT_FINAL', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel nom porte l''hymne national d''Espagne ?', 'MARCHA REAL', 'SPRINT_FINAL', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle science s''occupe des déchets et de leur élimination ?', 'RUDOLOGIE', 'SPRINT_FINAL', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Comment appelle-t-on les habitants de CHAMONIX ?', 'Les CHAMONIARDS', 'SPRINT_FINAL', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel titan déroba le feu du ciel afin de le donner aux hommes ?', 'Prométhée', 'SPRINT_FINAL', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel grand serpent Apollon tue-t-il à Delphes pour fonder un oracle ?', 'Le Python', 'SPRINT_FINAL', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel écrivain espagnol a écrit « La Maison de BERNADA » ?', 'Federico GARCIA LORCA', 'SPRINT_FINAL', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel était l''ancien nom de l''Hôtel-de-Ville à Paris ?', 'Place de la Grève', 'SPRINT_FINAL', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel nom désigne pour les latino-américains, un étranger venu des USA ?', 'GRINGO', 'SPRINT_FINAL', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- CASCADE Questions (10 questions éclairs)
-- =====================================================

INSERT INTO ms_question (id, text_fr, answer, round_type, difficulty, question_format, active, usage_count, success_count, source, created_at, updated_at)
VALUES
(gen_random_uuid(), 'Dans quelle ville peut-on observer la petite sirène ?', 'COPENHAGUE', 'CASCADE', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle est la devise de l''organisation SOS Racisme ?', 'Touche pas à mon pote', 'CASCADE', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'A quel sport collectif associez-vous le mot « Manchette » ?', 'VOLLEYBALL', 'CASCADE', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Qui construisit, en 1714, le premier thermomètre à mercure ?', 'FAHRENHEIT', 'CASCADE', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Comment appelle-t-on le tableau de nombres rangés dans un carré où les sommes sont égales ?', 'CARRÉ MAGIQUE', 'CASCADE', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel animal est surnommé « la vache des pauvres » ?', 'LA CHÈVRE', 'CASCADE', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle héroïne de Léon TOLSTOÏ, se jette sous les roues d''un train ?', 'ANNA KARÉNINE', 'CASCADE', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Comment appelle-t-on l''étude et l''exploration scientifique des grottes ?', 'SPÉLÉOLOGIE', 'CASCADE', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel était le nom donné au Cap de Bonne Espérance à sa découverte ?', 'CAP DES TEMPÊTES', 'CASCADE', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle ville est desservie par l''aéroport MARCO POLO ?', 'VENISE', 'CASCADE', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- CROSS_COUNTRY Questions (lieux géographiques)
-- =====================================================

INSERT INTO ms_question (id, text_fr, answer, round_type, difficulty, question_format, active, usage_count, success_count, source, created_at, updated_at)
VALUES
(gen_random_uuid(), 'Dans quelle ville trouve-t-on l''aéroport Cardinal Bernardin GANTIN ?', 'COTONOU', 'CROSS_COUNTRY', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel nom désigne cette ville de l''Argentine qui porte le nom de la monnaie du Nicaragua ?', 'CORDOBA', 'CROSS_COUNTRY', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle ville d''EUROPE est la capitale de la province de Biscaye ?', 'BILBAO', 'CROSS_COUNTRY', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle ville d''Océanie fondée en 1836 doit son nom à l''épouse du souverain britannique Guillaume IV ?', 'ADELAÏDE', 'CROSS_COUNTRY', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle ville des USA, réclamée par l''Espagne, obtint son ralliement aux USA le 23 Septembre 1810 ?', 'BATON ROUGE', 'CROSS_COUNTRY', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle ville résidentielle des USA est réputée pour les maisons luxueuses des personnalités du cinéma ?', 'BEVERLY HILLS', 'CROSS_COUNTRY', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle ville est la capitale spirituelle des fons, réputée pour sa culture de palmier à huile ?', 'ABOMEY', 'CROSS_COUNTRY', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'A quel pays associez-vous la ville de CHICOUTIMI ?', 'CANADA', 'CROSS_COUNTRY', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Dans quelle ville peut-on contempler le Taj Mahal ?', 'AGRA', 'CROSS_COUNTRY', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Dans quel Etat des USA trouve-t-on le Grand Canyon ?', 'ARIZONA', 'CROSS_COUNTRY', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- CROSS_DICTIONARY Questions (noms propres, lettre imposée)
-- =====================================================

INSERT INTO ms_question (id, text_fr, answer, round_type, difficulty, imposed_letter, question_format, active, usage_count, success_count, source, created_at, updated_at)
VALUES
(gen_random_uuid(), 'Quel nom porte le grand désert d''Asie ?', 'GOBI', 'CROSS_DICTIONARY', 'EASY', 'G', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle aventurière italienne, épouse de Concini, fut exécutée pour sorcellerie ?', 'Leonora GALIGAÏ', 'CROSS_DICTIONARY', 'HARD', 'G', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel sculpteur américain d''origine russe est célèbre pour ses sculptures à base de fils Nylon ?', 'Naum GABO', 'CROSS_DICTIONARY', 'HARD', 'G', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel tsar de Russie inspira à Pouchkine une tragédie et à MOUSSORGSKI un opéra ?', 'Boris GODOUNOV', 'CROSS_DICTIONARY', 'HARD', 'G', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- TIRS_AU_BUT Questions (mot à deviner)
-- =====================================================

INSERT INTO ms_question (id, text_fr, answer, round_type, difficulty, imposed_letter, question_format, active, usage_count, success_count, source, created_at, updated_at)
VALUES
(gen_random_uuid(), 'G 9 lettres : Souvent terminée par une main, c''est une baguette pour se gratter le dos ?', 'GRATTE-DOS', 'TIRS_AU_BUT', 'MEDIUM', 'G', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'A 7 lettres : Adjectif, qui déplaît et provoque un vif désagrément ?', 'AFFREUX', 'TIRS_AU_BUT', 'EASY', 'A', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'B 4 lettres : Dans le registre familier, c''est « moi » ; petit chapeau de dame ?', 'BIBI', 'TIRS_AU_BUT', 'EASY', 'B', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'B 6 lettres : Péjoratif et familier ; c''est une boisson de qualité médiocre ?', 'BIBINE', 'TIRS_AU_BUT', 'MEDIUM', 'B', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'C 6 lettres : Flanc d''une colline, petite colline, vignoble ?', 'COTEAU', 'TIRS_AU_BUT', 'MEDIUM', 'C', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'E 6 lettres : Squelette commercialisé pour absorber les liquides ?', 'EPONGE', 'TIRS_AU_BUT', 'EASY', 'E', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'F 8 lettres : Danse d''origine provençale où les participants se tiennent par la main ?', 'FARANDOLE', 'TIRS_AU_BUT', 'MEDIUM', 'F', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'G 7 lettres : Béton coloré, destiné aux revêtements, qui a l''aspect d''un granite ?', 'GRANITO', 'TIRS_AU_BUT', 'HARD', 'G', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- JACKPOT Questions (enchères avec indices)
-- =====================================================

INSERT INTO ms_question (id, text_fr, answer, hint_fr, round_type, difficulty, question_format, active, usage_count, success_count, source, created_at, updated_at)
VALUES
(gen_random_uuid(), 'Respectivement de couleurs rouge ou blanche, l''emblème des deux belligérants a donné le nom de ce conflit anglais ?', 'GUERRE DES DEUX-ROSES', 'Les amoureuses auraient aimé que cette malédiction tombe sur elles…', 'JACKPOT', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Cette fête aujourd''hui célèbre l''amour. Pour les bergers, c''était une fête en l''honneur de Faunus ?', 'LA SAINT-VALENTIN', 'J''ai été référée par les œuvres de Geoffrey CHAUCER, les Lupercales sont l''une de mes origines', 'JACKPOT', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Né à Garmsar en 1956. Docteur en Transport public, Président de l''Iran ?', 'Mahmoud AHMADINEJAD', 'Les USA n''apprécient pas ce « Shah » des temps modernes', 'JACKPOT', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- TRANSALT Questions (villes et pays du monde)
-- =====================================================

INSERT INTO ms_question (id, text_fr, answer, round_type, difficulty, question_format, active, usage_count, success_count, source, created_at, updated_at)
VALUES
(gen_random_uuid(), 'A quelle ville de Turquie associez-vous la Mosquée Bleue ?', 'ISTANBUL', 'TRANSALT', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Dans quelle ville d''Asie trouvez-vous la Mosquée du Vendredi ?', 'DELHI', 'TRANSALT', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'A quel pays associez-vous le Mackenzie ?', 'CANADA', 'TRANSALT', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle Guinée est encore appelée « La Guinée Espagnole » ?', 'GUINÉE ÉQUATORIALE', 'TRANSALT', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle plante est le symbole de l''Ecosse ?', 'LE CHARDON', 'TRANSALT', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'A quel pays associez-vous la ville Namur ?', 'BELGIQUE', 'TRANSALT', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel nom porte l''endroit le plus bas du monde ?', 'LA MER MORTE', 'TRANSALT', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle est la capitale de la Tchétchénie ?', 'GROZNY', 'TRANSALT', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel port d''Egypte, sur la méditerranée, est à l''entrée du canal de Suez ?', 'PORT-SAÏD', 'TRANSALT', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel était l''ancien nom de la Mer Noire ?', 'PONT-EUXIN', 'TRANSALT', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- Questions générales (utilisables dans plusieurs modes)
-- =====================================================

INSERT INTO ms_question (id, text_fr, answer, difficulty, question_format, active, usage_count, success_count, source, created_at, updated_at)
VALUES
(gen_random_uuid(), 'Quel écrivain est moins connu sous le nom d''Alexeï Maximovitch PECHKOV ?', 'MAXIME GORKI', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Qui est surnommé, en Amérique du Sud, El Libertador ?', 'SIMON BOLIVAR', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Qui a écrit Mémoires d''HADRIEN ?', 'MARGUERITE YOURCENAR', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel nom porte le bombardier B-29 qui largua la première bombe atomique sur HIROSHIMA ?', 'ENOLA GAY', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel nom porte l''abbaye fondée au Xe siècle sur un îlot rocheux de la Manche ?', 'MONT SAINT-MICHEL', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel film de Luchino VISCONTI obtint, à CANNES 1963, la palme d''or ?', 'LE GUÉPARD', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel système de triple étoiles est l''étoile la plus proche de la terre après le soleil ?', 'ALPHA CENTAURI', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel archéologue britannique découvre le palais de CNOSSOS en 1906 ?', 'ARTHUR EVANS', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Sous quel nom est plus connu l''homme de Java ?', 'PITHÉCANTHROPE', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Combien de cases y a-t-il sur un échiquier ?', '64', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle agence, créée en 1835, est l''ancêtre de l''Agence France Presse ?', 'AGENCE HAVAS', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel nom porte la doctrine philosophique qui prône la recherche systématique du plaisir ?', 'HÉDONISME', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Qui a écrit « Hostie Noire » ?', 'LÉOPOLD SÉDAR SENGHOR', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle est la langue officielle de l''État d''Israël ?', 'HÉBREU', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle planète est surnommée l''étoile du berger ?', 'VÉNUS', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Qui a assassiné en 1610, Henri IV ?', 'RAVAILLAC', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel est le vrai nom de Stendhal ?', 'HENRI BEYLE', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel centaure est l''éducateur d''Achille ?', 'CHIRON', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Qui a écrit TOPAZE ?', 'MARCEL PAGNOL', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel régime gouverna la France de 1795 à 1799 ?', 'LE DIRECTOIRE', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel personnage biblique vit sa femme transformée en bloc de sel ?', 'LOT', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel syndicat polonais créé en 1980 a été présidé par Lech WALESA ?', 'SOLIDARNOSC', 'MEDIUM', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle grande place publique d''Asie fut le théâtre des manifestations réprimées en 1989 ?', 'TIANANMEN', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quel athlète tchèque est surnommé la locomotive ?', 'EMIL ZÁTOPEK', 'HARD', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW()),
(gen_random_uuid(), 'Quelle ancienne ville cananéenne fut détruite avec Gomorrhe par le feu du ciel ?', 'SODOME', 'EASY', 'TEXT', true, 0, 0, 'Mindsoccer Zinsou', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- Verification: Count questions by round type
-- =====================================================

-- SELECT round_type, COUNT(*) as count
-- FROM ms_question
-- WHERE source = 'Mindsoccer Zinsou'
-- GROUP BY round_type
-- ORDER BY round_type;
