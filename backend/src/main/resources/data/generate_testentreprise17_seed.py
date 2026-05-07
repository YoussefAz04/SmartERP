#!/usr/bin/env python3
"""
Generate realistic ERP seed data for entreprise id=17.

Mirrors the dataset shape of testentreprise (id=15) / id=16 and additionally
emits purchase and sales invoices (facture_achat/facture_de_vente + lignes)
so the AI/ML pipeline (`MlForecastEngine`, `AnalyticService`, AI dashboards
that read factures) has end-to-end data to work with.

Schema corrections vs. id=16 seed:
  - `facture_de_vente` insert no longer references non-existent `created_at`
    / `updated_at` columns (the FactureDeVente entity has neither).
  - `facture_de_vente` insert now includes `montant_total` (FactureDeVente
    entity defines `montantTotal`).
  - All inserted columns have been cross-checked against
    backend/src/main/java/.../entity/*.java to match table layouts.

Outputs:
  - backend/src/main/resources/data/testentreprise17_seed.sql  (SQL Server)
  - backend/src/main/resources/data/testentreprise17_seed.json (inspection)
"""
import json, random
from datetime import date, datetime, timedelta
from pathlib import Path

random.seed(20260517)  # distinct seed from id=15 / id=16 datasets

ENT_ID = 17
TVA = 0.19  # Tunisian standard VAT
TODAY = date(2026, 5, 5)
START = date(2025, 9, 1)

# ID offsets: stay well clear of ent=15 (1500/5000) and ent=16 (1600/5100).
OFF_CAT_ART  = 1700
OFF_MESURE   = 1700
OFF_FRN      = 1700
OFF_CAT_CLI  = 1700
OFF_CLI      = 1700
OFF_ART      = 1700
OFF_ART_V    = 1700
OFF_CMD_A    = 1700
OFF_LIG_A    = 5200
OFF_CMD_V    = 1700
OFF_LIG_V    = 5200
OFF_FAC_A    = 1700
OFF_LFA      = 5200
OFF_FAC_V    = 1700
OFF_LFV      = 5200

REGIONS = ["Tunis", "Sfax", "Sousse", "Ariana", "Ben Arous", "Nabeul",
           "Bizerte", "Monastir", "Gabes", "Kairouan"]

CATEGORIES = [
    ("ELEC",  "Electronique",      "Composants et appareils electroniques", "piece"),
    ("INFO",  "Informatique",      "Materiel informatique et accessoires",  "piece"),
    ("BUR",   "Fournitures Bureau","Articles de bureau et papeterie",       "piece"),
    ("ALIM",  "Alimentaire",       "Produits alimentaires",                 "kg"),
    ("BOIS",  "Boissons",          "Boissons et eaux",                      "piece"),
    ("HYG",   "Hygiene",           "Produits d'hygiene",                    "piece"),
    ("MEN",   "Menager",           "Produits menagers",                     "piece"),
    ("TEX",   "Textile",           "Textile et habillement",                "piece"),
    ("EMB",   "Emballage",         "Cartons et emballages",                 "carton"),
    ("CONS",  "Consommables",      "Consommables divers",                   "piece"),
    ("OUTI",  "Outillage",         "Outils et accessoires",                 "piece"),
    ("CONST", "Construction",      "Materiaux de construction",             "kg"),
]

MESURES = [
    ("PCE", "Piece"),
    ("KG",  "Kilogramme"),
    ("L",   "Litre"),
    ("CRT", "Carton"),
    ("BTE", "Boite"),
    ("M",   "Metre"),
]

FRN_NAMES = [
    "Tunisie Distribution", "Maghreb Supply", "Carthage Trading", "Sahel Import",
    "Mediterranean Goods", "Atlas Wholesale", "El Manar Trading", "Sfax Logistics",
    "STE Tunisienne de Negoce", "Société Sousse Commerce", "Bizerte Import",
    "Nabeul Distribution",
]

CLIENT_FIRST_B2C = ["Mohamed","Ahmed","Ali","Youssef","Sami","Karim","Hedi","Slim",
                    "Amine","Khaled","Nour","Salma","Fatma","Leila","Sonia",
                    "Rim","Imen","Asma","Nadia","Mariem"]
CLIENT_LAST = ["Ben Ali","Trabelsi","Khelifi","Mansouri","Bouzid","Gharbi","Sassi",
               "Hammami","Jendoubi","Abidi","Kefi","Bouaziz","Saidi","Rekik","Ferchichi"]
CLIENT_B2B = ["SARL","SA","STE","Group","Boutique","Distrib","Trading"]
B2B_ANCHOR = ["Carthage","Medina","Marsa","Lac","Berges","Manar","Centre Ville",
              "Sahel","Cap Bon","Jasmin","Olive","Palmier"]

ARTICLE_TEMPLATES = {
    "ELEC":  [("Cable HDMI 2m",1.5,8,15),("Adaptateur USB-C",1.0,12,25),
              ("Multiprise 5 prises",2.0,25,45),("Lampe LED 9W",0.5,7,14),
              ("Pile AA pack 4",0.3,3,7),("Casque audio",0.4,35,80),
              ("Souris sans fil",0.3,18,38)],
    "INFO":  [("Clavier USB",1.2,40,75),("Cle USB 32GB",0.1,15,30),
              ("Disque dur ext 1TB",0.5,160,260),("Webcam HD",0.4,55,110),
              ("Hub USB 4 ports",0.2,22,42),("Tapis souris",0.1,5,12)],
    "BUR":   [("Ramette papier A4",2.5,12,22),("Stylo bille pack 10",0.2,4,9),
              ("Cahier 200p",0.5,5,11),("Classeur A4",0.7,8,16),
              ("Marqueur permanent",0.05,2,5),("Agrafeuse",0.4,15,30),
              ("Calculatrice",0.3,18,38)],
    "ALIM":  [("Huile olive 1L",1.0,18,28),("Sucre 1kg",1.0,3.5,5.5),
              ("Farine 5kg",5.0,12,18),("Riz 1kg",1.0,4,7),
              ("Pates 500g",0.5,2,4),("Tomate concentree 800g",0.8,5,9),
              ("Cafe 250g",0.25,12,22)],
    "BOIS":  [("Eau minerale 1.5L",1.5,1.0,2.0),("Soda 1.5L",1.5,2.5,4.5),
              ("Jus orange 1L",1.0,4,7),("The vert pack",0.2,8,15),
              ("Cafe en grain 500g",0.5,22,38)],
    "HYG":   [("Savon liquide 500ml",0.5,4,8),("Shampoing 400ml",0.4,7,14),
              ("Dentifrice 75ml",0.1,3.5,7),("Papier toilette x12",1.5,12,22),
              ("Mouchoirs pack 6",0.3,4,9)],
    "MEN":   [("Detergent 5L",5.0,15,28),("Eau javel 1L",1.0,2.5,5),
              ("Produit vaisselle 1L",1.0,4.5,9),("Eponge pack 6",0.1,2,5),
              ("Sac poubelle 100L pack",1.0,6,12)],
    "TEX":   [("T-shirt coton",0.2,8,22),("Polo pro brode",0.3,18,45),
              ("Tablier cuisine",0.3,12,28),("Serviette eponge",0.4,9,20)],
    "EMB":   [("Carton 40x30",0.3,1.5,3.5),("Film etirable 2kg",2.0,18,32),
              ("Adhesif 50m",0.2,3,7),("Pochette bulle pack 50",1.0,15,28)],
    "CONS":  [("Cartouche encre noire",0.1,45,90),("Toner laser",0.7,120,220),
              ("Ruban LTO",0.1,55,110),("Etiquette pack 1000",0.5,8,18)],
    "OUTI":  [("Tournevis cruciforme",0.2,5,12),("Marteau 500g",0.5,12,28),
              ("Cle a molette",0.4,18,38),("Metre ruban 5m",0.2,7,16),
              ("Niveau a bulle 60cm",0.5,22,45)],
    "CONST": [("Ciment 25kg",25.0,12,18),("Plâtre 10kg",10.0,8,14),
              ("Peinture blanche 5L",5.0,38,68),("Carrelage m2",1.0,22,42)],
}

PAYMENT_MODES = ["VIREMENT", "ESPECES", "CHEQUE", "CARTE_BANCAIRE", "TRAITE"]


def q2(x): return round(x + 1e-9, 2)
def sql_str(s): return "'" + str(s).replace("'", "''") + "'"
def sql_date(d): return f"'{d.isoformat()}'"
def sql_dt(dt): return f"'{dt.isoformat(' ', 'seconds')}'"

# ---------------- Generate ----------------
json_data = {"entreprise_id": ENT_ID}

now_dt = datetime.combine(TODAY, datetime.min.time())
NOW = sql_dt(now_dt)

# 1. CategorieArticle
cat_arts = []
for i,(code,lib,desc,_) in enumerate(CATEGORIES):
    cat_arts.append({"id": OFF_CAT_ART+i, "code": code, "libelle": lib,
                     "description": desc})
json_data["categorie_article"] = cat_arts

# 2. Mesure
mesures = []
for i,(c,l) in enumerate(MESURES):
    mesures.append({"id": OFF_MESURE+i, "code": c, "libelle": l})
json_data["mesure"] = mesures

def compute_notation_from_taux(taux_pct):
    """Map taux de livraison (0-100 %) to a 1-5 star notation, matching FournisseurService logic."""
    if taux_pct >= 90: return 5.0
    if taux_pct >= 70: return 4.0
    if taux_pct >= 50: return 3.0
    if taux_pct >= 30: return 2.0
    return 1.0

# 3. Fournisseurs (12)
fournisseurs = []
for i,name in enumerate(FRN_NAMES):
    region = random.choice(REGIONS)
    taux_liv = round(random.uniform(0.7, 0.99), 2)
    # notation stored on 1-5 scale to match FournisseurService.computeNotationFromTaux
    notation = compute_notation_from_taux(taux_liv * 100)
    fournisseurs.append({
        "id": OFF_FRN+i, "nom": name, "prenom": "",
        "email": f"contact{i+1}@{name.split()[0].lower()}-e{ENT_ID}.tn",
        "telephone": f"+216 {random.randint(20,99)} {random.randint(100,999)} {random.randint(100,999)}",
        "adresse": f"{random.randint(1,200)} Rue {random.choice(['Habib Bourguiba','de la Liberte','de Carthage','Mohamed V'])}",
        "ville": region, "pays": "Tunisie", "code_postal": f"{random.randint(1000,9999)}",
        "secteur_activite": "Distribution", "taux_livraison": taux_liv,
        "notation": notation,
        "numero_identification_fiscale": f"{random.randint(1000000,9999999)}/A/M/000",
        "registre_commerce": f"B{random.randint(100000,999999)}2024",
    })
json_data["fournisseurs"] = fournisseurs

# 4. CategorieClient
cat_clis = [
    {"id": OFF_CAT_CLI+0, "nom": "B2B Grossiste",  "description": "Clients professionnels, gros volumes"},
    {"id": OFF_CAT_CLI+1, "nom": "B2B Detaillant", "description": "Boutiques et petites entreprises"},
    {"id": OFF_CAT_CLI+2, "nom": "B2C Premium",    "description": "Particuliers fideles"},
    {"id": OFF_CAT_CLI+3, "nom": "B2C Standard",   "description": "Particuliers"},
]
json_data["categorie_client"] = cat_clis

# 5. Clients (30)
clients = []
for i in range(30):
    is_b2b = i < 12
    if is_b2b:
        nom = f"{random.choice(CLIENT_B2B)} {random.choice(B2B_ANCHOR)}"
        prenom = "Service Achats"
        cat_id = OFF_CAT_CLI+0 if i < 6 else OFF_CAT_CLI+1
        ctype = "B2B"
    else:
        nom = random.choice(CLIENT_LAST)
        prenom = random.choice(CLIENT_FIRST_B2C)
        cat_id = OFF_CAT_CLI+2 if i < 24 else OFF_CAT_CLI+3
        ctype = "B2C"
    region = random.choice(REGIONS)
    clients.append({
        "id": OFF_CLI+i, "nom": nom, "prenom": prenom, "type": ctype,
        "email": f"client{i+1}-e{ENT_ID}@example.tn",
        "telephone": f"+216 {random.randint(20,99)} {random.randint(100,999)} {random.randint(100,999)}",
        "adresse": f"{random.randint(1,300)} Avenue {random.choice(['Bourguiba','de la Republique','Hedi Chaker'])}",
        "secteur_activite": "Commerce" if is_b2b else "Particulier",
        "categorie_client_id": cat_id, "region": region,
    })
json_data["clients"] = clients

# 6. Articles (Achat) + ArticleVente (sales mirror)
articles = []
article_ventes = []
art_seq = 0
for c_idx,(code,_,_,unit_default) in enumerate(CATEGORIES):
    templates = ARTICLE_TEMPLATES[code]
    n = random.randint(5, 7)
    for k in range(n):
        tpl = templates[k % len(templates)]
        suffix = "" if k < len(templates) else f" v{k+1}"
        nom, weight, p_min, p_max = tpl
        purchase = round(random.uniform(p_min, p_max), 2)
        margin_pct = random.uniform(0.18, 0.55)
        selling = q2(purchase * (1 + margin_pct))
        margin = q2((selling - purchase) / purchase * 100)
        frn_id = OFF_FRN + random.randint(0, len(FRN_NAMES)-1)
        cat_id = OFF_CAT_ART + c_idx
        mesure_idx = {"piece":0,"kg":1,"L":2,"carton":3,"box":4,"m":5}.get(unit_default,0)
        mesure_id = OFF_MESURE + mesure_idx
        aid = OFF_ART + art_seq
        avid = OFF_ART_V + art_seq
        ref = f"ART-{ENT_ID}-{aid:05d}"
        sku = f"SKU-{code}-E{ENT_ID}-{art_seq+1:03d}"
        articles.append({
            "id": aid, "article_vente_id": avid, "reference": ref,
            "nom": nom + suffix, "category_code": code, "category_id": cat_id,
            "purchase_price": purchase, "selling_price": selling,
            "margin_pct": margin, "supplier_id": frn_id,
            "unit": unit_default, "sku": sku, "mesure_id": mesure_id,
        })
        article_ventes.append({
            "id": avid, "article_achat_id": aid, "nom": nom + suffix,
            "prix_unitaire": selling, "quantite": 0,
            "unite": unit_default, "sku": sku,
            "markup": q2(margin),
        })
        art_seq += 1

# Mark best/low performers
random.shuffle(articles)
for a in articles[:3]:
    a["performance"] = "best"
for a in articles[3:6]:
    a["performance"] = "low"
for a in articles[6:]:
    a["performance"] = "normal"
articles.sort(key=lambda a: a["id"])
json_data["articles"]       = articles
json_data["article_ventes"] = article_ventes

best_ids = [a["id"] for a in articles if a["performance"]=="best"]
low_ids  = [a["id"] for a in articles if a["performance"]=="low"]


def month_seasonality(d: date) -> float:
    m = d.month
    summer = {6:1.25, 7:1.40, 8:1.35}.get(m, 1.0)
    rmd = 1.30 if (d.year==2026 and d.month==2 and d.day>=15) or (d.year==2026 and d.month==3 and d.day<=15) else 1.0
    school = 1.15 if m==9 else 1.0
    eoy = 1.10 if m==12 else 1.0
    return summer * rmd * school * eoy

def trend(d: date) -> float:
    months_in = (d.year - START.year)*12 + (d.month - START.month)
    return 1.0 + 0.02 * months_in

# 7. Purchase orders
commandes_achat = []
lignes_achat    = []
ca_seq = 0
la_seq = 0
cur = START
while cur < TODAY.replace(day=1):
    next_month = cur.replace(day=15) + timedelta(days=30)
    seasonal = month_seasonality(next_month)
    n_orders = max(3, int(round(random.uniform(4,7) * seasonal)))
    for _ in range(n_orders):
        d = cur + timedelta(days=random.randint(0,27))
        if d >= TODAY: break
        frn = random.choice(fournisseurs)
        cand = [a for a in articles if a["supplier_id"]==frn["id"]]
        if not cand: cand = articles
        n_lines = random.randint(2, 6)
        chosen = random.sample(cand, min(n_lines, len(cand)))
        cmd_id = OFF_CMD_A + ca_seq
        ca_seq += 1
        ref = f"CA-{ENT_ID}-{d.year}{d.month:02d}-{cmd_id:05d}"
        delivery = d + timedelta(days=random.randint(3,10))
        statut = "RECUE" if delivery < TODAY else "EN_COURS"
        montant_ht = 0.0
        order_lines = []
        for a in chosen:
            base_qty = 80 if a["performance"]=="best" else (15 if a["performance"]=="low" else 40)
            qty = max(5, int(base_qty * seasonal * random.uniform(0.6, 1.4)))
            qty = min(qty, 500)
            up = q2(a["purchase_price"] * random.uniform(0.95, 1.05))
            line_ht = q2(qty * up)
            line_ttc = q2(line_ht * (1+TVA))
            lid = OFF_LIG_A + la_seq
            la_seq += 1
            lignes_achat.append({
                "id": lid, "commande_achat_id": cmd_id, "article_id": a["id"],
                "article_nom": a["nom"],
                "quantite": qty, "prix_unitaire_ht": up, "tva": q2(TVA*100),
                "montant_ht": line_ht, "montant_ttc": line_ttc,
            })
            order_lines.append({"product_id":a["id"],"quantity":qty,"unit_price":up,"name":a["nom"]})
            montant_ht += line_ht
        montant_ht  = q2(montant_ht)
        montant_tva = q2(montant_ht * TVA)
        montant_ttc = q2(montant_ht + montant_tva)
        commandes_achat.append({
            "id": cmd_id, "reference": ref, "supplier_id": frn["id"],
            "date": d.isoformat(), "delivery_date": delivery.isoformat(),
            "statut": statut, "montant_ht": montant_ht,
            "montant_tva": montant_tva, "montant_ttc": montant_ttc,
            "lines": order_lines,
        })
    if cur.month == 12: cur = date(cur.year+1, 1, 1)
    else: cur = date(cur.year, cur.month+1, 1)

json_data["commandes_achat"] = commandes_achat

# 8. Sales orders
commandes_vente = []
lignes_vente    = []
cv_seq = 0
lv_seq = 0
cur = START
while cur < TODAY.replace(day=1):
    seasonal = month_seasonality(cur.replace(day=15))
    growth   = trend(cur)
    base_orders = random.uniform(13, 22)
    n_orders = max(6, int(round(base_orders * seasonal * growth)))
    if random.random() < 0.15:
        n_orders = int(n_orders * random.choice([0.5, 1.6]))
    for _ in range(n_orders):
        d = cur + timedelta(days=random.randint(0,27))
        if d >= TODAY: break
        cli = random.choice(clients)
        is_b2b = cli["type"] == "B2B"
        n_lines = random.randint(2, 5) if not is_b2b else random.randint(3, 7)
        weights = []
        for a in articles:
            w = 4.0 if a["performance"]=="best" else (0.4 if a["performance"]=="low" else 1.0)
            weights.append(w)
        chosen_idx = set()
        attempts = 0
        while len(chosen_idx) < n_lines and attempts < 50:
            attempts += 1
            r = random.random()*sum(weights)
            cum = 0
            for j,w in enumerate(weights):
                cum += w
                if r <= cum:
                    chosen_idx.add(j); break
        chosen = [articles[j] for j in chosen_idx]
        cmd_id = OFF_CMD_V + cv_seq
        cv_seq += 1
        ref = f"CV-{ENT_ID}-{d.year}{d.month:02d}-{cmd_id:05d}"
        delivery = d + timedelta(days=random.randint(1,5))
        statut = "LIVREE" if delivery < TODAY else "EN_COURS"
        montant_ht = 0.0
        order_lines = []
        for a in chosen:
            base_qty = 12 if a["performance"]=="best" else (2 if a["performance"]=="low" else 5)
            mult = 4 if is_b2b else 1
            qty = max(1, int(base_qty * mult * seasonal * growth * random.uniform(0.7, 1.4)))
            qty = min(qty, 200)
            disc = random.uniform(0.92, 1.0) if is_b2b else random.uniform(0.97, 1.03)
            sp = q2(a["selling_price"] * disc)
            if sp <= a["purchase_price"]:
                sp = q2(a["purchase_price"] * 1.10)
            line_ht = q2(qty * sp)
            line_ttc = q2(line_ht * (1+TVA))
            lid = OFF_LIG_V + lv_seq
            lv_seq += 1
            lignes_vente.append({
                "id": lid, "commande_vente_id": cmd_id,
                "article_vente_id": a["article_vente_id"],
                "article_nom": a["nom"],
                "quantite": qty, "prix_unitaire_ht": sp, "tva": q2(TVA*100),
                "montant_ht": line_ht, "montant_ttc": line_ttc,
            })
            order_lines.append({"product_id":a["id"],"quantity":qty,"selling_price":sp,"name":a["nom"]})
            montant_ht += line_ht
        montant_ht  = q2(montant_ht)
        montant_tva = q2(montant_ht * TVA)
        montant_ttc = q2(montant_ht + montant_tva)
        commandes_vente.append({
            "id": cmd_id, "reference": ref, "client_id": cli["id"],
            "date": d.isoformat(), "delivery_date": delivery.isoformat(),
            "statut": statut, "montant_ht": montant_ht,
            "montant_tva": montant_tva, "montant_ttc": montant_ttc,
            "lines": order_lines,
        })
    if cur.month == 12: cur = date(cur.year+1, 1, 1)
    else: cur = date(cur.year, cur.month+1, 1)

json_data["commandes_vente"] = commandes_vente

# 9. Article vente quantite from purchases - sales (proxy stock movement)
sold = {}
for lv in lignes_vente:
    sold[lv["article_vente_id"]] = sold.get(lv["article_vente_id"], 0) + int(lv["quantite"])
purchased = {}
for la in lignes_achat:
    purchased[la["article_id"]] = purchased.get(la["article_id"], 0) + int(la["quantite"])
for av in article_ventes:
    aid = av["article_achat_id"]
    av["quantite"] = max(0, purchased.get(aid,0) - sold.get(av["id"],0))

# 10. Factures Achat - one per RECUE commande_achat (issued at delivery)
factures_achat = []
lignes_facture_achat = []
fa_seq = 0
lfa_seq = 0
# group lignes by commande
la_by_cmd = {}
for la in lignes_achat:
    la_by_cmd.setdefault(la["commande_achat_id"], []).append(la)

for c in commandes_achat:
    if c["statut"] != "RECUE":
        continue
    fid = OFF_FAC_A + fa_seq
    fa_seq += 1
    fdate = date.fromisoformat(c["delivery_date"])
    ref = f"FA-{ENT_ID}-{fdate.year}{fdate.month:02d}-{fid:05d}"
    paid_chance = random.random()
    if paid_chance < 0.75:
        statut = "PAYEE"
    elif paid_chance < 0.92:
        statut = "EN_ATTENTE"
    else:
        statut = "PARTIELLEMENT_PAYEE"
    mode = random.choice(PAYMENT_MODES)
    factures_achat.append({
        "id": fid, "reference": ref, "date_achat": fdate.isoformat(),
        "commande_achat_id": c["id"], "fournisseur_id": c["supplier_id"],
        "montant_ht": c["montant_ht"], "montant_tva": c["montant_tva"],
        "montant_ttc": c["montant_ttc"], "montant_total": c["montant_ttc"],
        "remise": 0.0, "mode_paiement": mode, "statut": statut,
        "description": f"Facture pour commande {c['reference']}",
    })
    for la in la_by_cmd.get(c["id"], []):
        lid = OFF_LFA + lfa_seq
        lfa_seq += 1
        lignes_facture_achat.append({
            "id": lid, "facture_achat_id": fid,
            "designation": la["article_nom"],
            "quantite": la["quantite"],
            "prix_unitaire_ht": la["prix_unitaire_ht"],
            "tva": la["tva"],
            "montant_ht": la["montant_ht"],
            "montant_ttc": la["montant_ttc"],
        })

json_data["factures_achat"]        = factures_achat
json_data["lignes_facture_achat"]  = lignes_facture_achat

# 11. Factures Vente - one per LIVREE commande_vente
factures_vente = []
lignes_facture_vente = []
fv_seq = 0
lfv_seq = 0
lv_by_cmd = {}
for lv in lignes_vente:
    lv_by_cmd.setdefault(lv["commande_vente_id"], []).append(lv)

for c in commandes_vente:
    if c["statut"] != "LIVREE":
        continue
    fid = OFF_FAC_V + fv_seq
    fv_seq += 1
    fdate = date.fromisoformat(c["delivery_date"])
    ref = f"FV-{ENT_ID}-{fdate.year}{fdate.month:02d}-{fid:05d}"
    paid_chance = random.random()
    if paid_chance < 0.80:
        statut = "PAYEE"
    elif paid_chance < 0.95:
        statut = "EN_ATTENTE"
    else:
        statut = "PARTIELLEMENT_PAYEE"
    mode = random.choice(PAYMENT_MODES)
    factures_vente.append({
        "id": fid, "reference": ref, "date_vente": fdate.isoformat(),
        "commande_vente_id": c["id"], "client_id": c["client_id"],
        "montant_ht": c["montant_ht"], "montant_tva": c["montant_tva"],
        "montant_ttc": c["montant_ttc"],
        "remise": 0.0, "mode_paiement": mode, "statut": statut,
        "description": f"Facture pour commande {c['reference']}",
    })
    for lv in lv_by_cmd.get(c["id"], []):
        lid = OFF_LFV + lfv_seq
        lfv_seq += 1
        lignes_facture_vente.append({
            "id": lid, "facture_de_vente_id": fid,
            "designation": lv["article_nom"],
            "quantite": lv["quantite"],
            "prix_unitaire_ht": lv["prix_unitaire_ht"],
            "tva": lv["tva"],
            "montant_ht": lv["montant_ht"],
            "montant_ttc": lv["montant_ttc"],
        })

json_data["factures_vente"]        = factures_vente
json_data["lignes_facture_vente"]  = lignes_facture_vente

# ---------------- SQL output ----------------
def sql_header():
    return [
        f"-- Seed data for entreprise id={ENT_ID}",
        "-- Generated by generate_testentreprise17_seed.py - SQL Server dialect",
        "-- All IDs are explicit and start at 1700/5200 to avoid clashes with",
        "-- the testentreprise (id=15) seed (1500/5000) and id=16 seed (1600/5100).",
        "-- Run inside a transaction; review SET IDENTITY_INSERT statements.",
        "",
        "-- The article / article_vente tables have filtered unique indexes",
        "-- (e.g. on `reference IS NOT NULL`, `stock_id IS NOT NULL`) which",
        "-- require QUOTED_IDENTIFIER ON for any DML.",
        "SET QUOTED_IDENTIFIER ON;",
        "SET ANSI_NULLS ON;",
        "",
        f"DECLARE @entId BIGINT = {ENT_ID};",
        "",
        f"IF NOT EXISTS (SELECT 1 FROM entreprise WHERE id = @entId)",
        f"    RAISERROR('Entreprise id={ENT_ID} does not exist. Create it before running this script.', 16, 1);",
        "",
    ]

L = sql_header()

# CategorieArticle
L.append("SET IDENTITY_INSERT categorie_article ON;")
for c in cat_arts:
    L.append(
        f"INSERT INTO categorie_article (id, code, libelle, description, created_at, updated_at, entreprise_id) "
        f"VALUES ({c['id']}, {sql_str(c['code'])}, {sql_str(c['libelle'])}, {sql_str(c['description'])}, {NOW}, {NOW}, @entId);"
    )
L += ["SET IDENTITY_INSERT categorie_article OFF;",""]

# Mesure
L.append("SET IDENTITY_INSERT mesure ON;")
for m in mesures:
    L.append(
        f"INSERT INTO mesure (id, code, libelle, created_at, updated_at, entreprise_id) "
        f"VALUES ({m['id']}, {sql_str(m['code'])}, {sql_str(m['libelle'])}, {NOW}, {NOW}, @entId);"
    )
L += ["SET IDENTITY_INSERT mesure OFF;",""]

# Fournisseur
L.append("SET IDENTITY_INSERT fournisseur ON;")
for f in fournisseurs:
    L.append(
        "INSERT INTO fournisseur (id, nom, prenom, email, telephone, adresse, secteur_activite, "
        "taux_livraison, notation, ville, pays, code_postal, numero_identification_fiscale, "
        "registre_commerce, created_at, updated_at, entreprise_id) VALUES "
        f"({f['id']}, {sql_str(f['nom'])}, {sql_str(f['prenom'])}, {sql_str(f['email'])}, "
        f"{sql_str(f['telephone'])}, {sql_str(f['adresse'])}, {sql_str(f['secteur_activite'])}, "
        f"{f['taux_livraison']}, {f['notation']}, {sql_str(f['ville'])}, {sql_str(f['pays'])}, "
        f"{sql_str(f['code_postal'])}, {sql_str(f['numero_identification_fiscale'])}, "
        f"{sql_str(f['registre_commerce'])}, {NOW}, {NOW}, @entId);"
    )
L += ["SET IDENTITY_INSERT fournisseur OFF;",""]

# CategorieClient
L.append("SET IDENTITY_INSERT categorie_client ON;")
for c in cat_clis:
    L.append(
        f"INSERT INTO categorie_client (id, nom, description) "
        f"VALUES ({c['id']}, {sql_str(c['nom'])}, {sql_str(c['description'])});"
    )
L += ["SET IDENTITY_INSERT categorie_client OFF;",""]

# Client
L.append("SET IDENTITY_INSERT client ON;")
for c in clients:
    L.append(
        "INSERT INTO client (id, nom, prenom, email, telephone, adresse, secteur_activite, "
        "categorie_client_id, entreprise_id) VALUES "
        f"({c['id']}, {sql_str(c['nom'])}, {sql_str(c['prenom'])}, {sql_str(c['email'])}, "
        f"{sql_str(c['telephone'])}, {sql_str(c['adresse'])}, {sql_str(c['secteur_activite'])}, "
        f"{c['categorie_client_id']}, @entId);"
    )
L += ["SET IDENTITY_INSERT client OFF;",""]

# Article
L.append("SET IDENTITY_INSERT article ON;")
for a in articles:
    L.append(
        "INSERT INTO article (id, reference, nom, description, prix_achat, prix_vente, "
        "quantite, sku, unite, actif, created_at, updated_at, categorie_article_id, "
        "mesure_id, fournisseur_id, entreprise_id) VALUES "
        f"({a['id']}, {sql_str(a['reference'])}, {sql_str(a['nom'])}, "
        f"{sql_str(a['nom']+' - '+a['category_code'])}, {a['purchase_price']}, {a['selling_price']}, "
        f"0, {sql_str(a['sku'])}, {sql_str(a['unit'])}, 1, {NOW}, {NOW}, "
        f"{a['category_id']}, {a['mesure_id']}, {a['supplier_id']}, @entId);"
    )
L += ["SET IDENTITY_INSERT article OFF;",""]

# ArticleVente
L.append("SET IDENTITY_INSERT article_vente ON;")
for av in article_ventes:
    L.append(
        "INSERT INTO article_vente (id, nom, description, prix_unitaire, quantite, unite, sku, "
        "markup, created_at, updated_at, article_achat_id) VALUES "
        f"({av['id']}, {sql_str(av['nom'])}, NULL, {av['prix_unitaire']}, {av['quantite']}, "
        f"{sql_str(av['unite'])}, {sql_str(av['sku'])}, {av['markup']}, {NOW}, {NOW}, "
        f"{av['article_achat_id']});"
    )
L += ["SET IDENTITY_INSERT article_vente OFF;",""]

# CommandeAchat
L.append("SET IDENTITY_INSERT commande_achat ON;")
for c in commandes_achat:
    L.append(
        "INSERT INTO commande_achat (id, reference, date_commande, date_livraison_prevue, statut, "
        "montant_ht, montant_tva, montant_ttc, note, created_at, updated_at, fournisseur_id, entreprise_id) VALUES "
        f"({c['id']}, {sql_str(c['reference'])}, {sql_date(date.fromisoformat(c['date']))}, "
        f"{sql_date(date.fromisoformat(c['delivery_date']))}, {sql_str(c['statut'])}, "
        f"{c['montant_ht']}, {c['montant_tva']}, {c['montant_ttc']}, NULL, {NOW}, {NOW}, "
        f"{c['supplier_id']}, @entId);"
    )
L += ["SET IDENTITY_INSERT commande_achat OFF;",""]

L.append("SET IDENTITY_INSERT ligne_commande_achat ON;")
for la in lignes_achat:
    L.append(
        "INSERT INTO ligne_commande_achat (id, quantite, prix_unitaire_ht, taux_tva, montant_ht, "
        "montant_ttc, created_at, updated_at, article_id, commande_achat_id) VALUES "
        f"({la['id']}, {la['quantite']}, {la['prix_unitaire_ht']}, {la['tva']}, {la['montant_ht']}, "
        f"{la['montant_ttc']}, {NOW}, {NOW}, {la['article_id']}, {la['commande_achat_id']});"
    )
L += ["SET IDENTITY_INSERT ligne_commande_achat OFF;",""]

# BonReception – one per RECUE commande_achat, date reflects fournisseur reliability
bons_reception = []
br_seq = 0
frn_taux_by_id = {f["id"]: f["taux_livraison"] for f in fournisseurs}
for c in commandes_achat:
    if c["statut"] != "RECUE":
        continue
    ca_id = c["id"]
    dlp = date.fromisoformat(c["delivery_date"])
    frn_id = c["supplier_id"]
    taux = frn_taux_by_id.get(frn_id, 0.85)
    pseudo_rand = ((ca_id * 7 + 13) % 100) / 100.0
    on_time = pseudo_rand < taux
    reception_date = dlp - timedelta(days=1) if on_time else dlp + timedelta(days=3)
    if reception_date >= TODAY:
        reception_date = TODAY - timedelta(days=1)
    bons_reception.append({
        "id": OFF_FRN + br_seq,
        "reference": f"BR-CA-{ca_id:05d}",
        "date_reception": reception_date.isoformat(),
        "commande_achat_id": ca_id,
    })
    br_seq += 1

L.append("SET IDENTITY_INSERT bon_reception ON;")
for br in bons_reception:
    L.append(
        "INSERT INTO bon_reception (id, reference, date_reception, statut, note, created_at, updated_at, commande_achat_id, entreprise_id) VALUES "
        f"({br['id']}, {sql_str(br['reference'])}, {sql_date(date.fromisoformat(br['date_reception']))}, "
        f"'COMPLET', NULL, {NOW}, {NOW}, {br['commande_achat_id']}, @entId);"
    )
L += ["SET IDENTITY_INSERT bon_reception OFF;",""]
L.append("SET IDENTITY_INSERT commande_vente ON;")
for c in commandes_vente:
    L.append(
        "INSERT INTO commande_vente (id, reference, date_commande, date_livraison_prevue, statut, "
        "montant_ht, montant_tva, montant_ttc, note, created_at, updated_at, client_id, entreprise_id) VALUES "
        f"({c['id']}, {sql_str(c['reference'])}, {sql_date(date.fromisoformat(c['date']))}, "
        f"{sql_date(date.fromisoformat(c['delivery_date']))}, {sql_str(c['statut'])}, "
        f"{c['montant_ht']}, {c['montant_tva']}, {c['montant_ttc']}, NULL, {NOW}, {NOW}, "
        f"{c['client_id']}, @entId);"
    )
L += ["SET IDENTITY_INSERT commande_vente OFF;",""]

L.append("SET IDENTITY_INSERT ligne_commande_vente ON;")
for lv in lignes_vente:
    L.append(
        "INSERT INTO ligne_commande_vente (id, quantite, prix_unitaire_ht, taux_tva, montant_ht, "
        "montant_ttc, created_at, updated_at, article_id, commande_vente_id) VALUES "
        f"({lv['id']}, {lv['quantite']}, {lv['prix_unitaire_ht']}, {lv['tva']}, {lv['montant_ht']}, "
        f"{lv['montant_ttc']}, {NOW}, {NOW}, {lv['article_vente_id']}, {lv['commande_vente_id']});"
    )
L += ["SET IDENTITY_INSERT ligne_commande_vente OFF;",""]

# FactureAchat
L.append("SET IDENTITY_INSERT facture_achat ON;")
for f in factures_achat:
    L.append(
        "INSERT INTO facture_achat (id, referance, date_achat, montant_total, montant_ht, "
        "montant_tva, montant_ttc, remise, mode_paiement, statut, description, chemin_fichier, "
        "fournisseur_id, commande_achat_id, entreprise_id) VALUES "
        f"({f['id']}, {sql_str(f['reference'])}, {sql_str(f['date_achat'])}, "
        f"{f['montant_total']}, {f['montant_ht']}, {f['montant_tva']}, {f['montant_ttc']}, "
        f"{f['remise']}, {sql_str(f['mode_paiement'])}, {sql_str(f['statut'])}, "
        f"{sql_str(f['description'])}, NULL, {f['fournisseur_id']}, "
        f"{f['commande_achat_id']}, @entId);"
    )
L += ["SET IDENTITY_INSERT facture_achat OFF;",""]

L.append("SET IDENTITY_INSERT ligne_facture_achat ON;")
for la in lignes_facture_achat:
    L.append(
        "INSERT INTO ligne_facture_achat (id, article_nom, quantite, prix_unitaire_ht, taux_tva, "
        "montant_ht, montant_ttc, created_at, updated_at, facture_achat_id) VALUES "
        f"({la['id']}, {sql_str(la['designation'])}, {la['quantite']}, {la['prix_unitaire_ht']}, "
        f"{la['tva']}, {la['montant_ht']}, {la['montant_ttc']}, {NOW}, {NOW}, "
        f"{la['facture_achat_id']});"
    )
L += ["SET IDENTITY_INSERT ligne_facture_achat OFF;",""]

# FactureVente (table: facture_de_vente)
L.append("SET IDENTITY_INSERT facture_de_vente ON;")
for f in factures_vente:
    L.append(
        "INSERT INTO facture_de_vente (id, referance, date_achat, montant_total, montant_ht, "
        "montant_tva, montant_ttc, remise, mode_paiement, statut, description, chemin_fichier, "
        "client_id, commande_vente_id, entreprise_id) VALUES "
        f"({f['id']}, {sql_str(f['reference'])}, {sql_str(f['date_vente'])}, "
        f"{f['montant_ttc']}, {f['montant_ht']}, {f['montant_tva']}, {f['montant_ttc']}, "
        f"{f['remise']}, {sql_str(f['mode_paiement'])}, {sql_str(f['statut'])}, "
        f"{sql_str(f['description'])}, NULL, {f['client_id']}, "
        f"{f['commande_vente_id']}, @entId);"
    )
L += ["SET IDENTITY_INSERT facture_de_vente OFF;",""]

L.append("SET IDENTITY_INSERT ligne_facture_vente ON;")
for lv in lignes_facture_vente:
    L.append(
        "INSERT INTO ligne_facture_vente (id, article_nom, quantite, prix_unitaire_ht, taux_tva, "
        "montant_ht, montant_ttc, created_at, updated_at, facture_de_vente_id) VALUES "
        f"({lv['id']}, {sql_str(lv['designation'])}, {lv['quantite']}, {lv['prix_unitaire_ht']}, "
        f"{lv['tva']}, {lv['montant_ht']}, {lv['montant_ttc']}, {NOW}, {NOW}, "
        f"{lv['facture_de_vente_id']});"
    )
L += ["SET IDENTITY_INSERT ligne_facture_vente OFF;",""]

# Summary
counts = {
    "categorie_article": len(cat_arts), "mesure": len(mesures),
    "fournisseur": len(fournisseurs), "categorie_client": len(cat_clis),
    "client": len(clients), "article": len(articles),
    "article_vente": len(article_ventes),
    "commande_achat": len(commandes_achat),
    "ligne_commande_achat": len(lignes_achat),
    "commande_vente": len(commandes_vente),
    "ligne_commande_vente": len(lignes_vente),
    "facture_achat": len(factures_achat),
    "ligne_facture_achat": len(lignes_facture_achat),
    "facture_de_vente": len(factures_vente),
    "ligne_facture_vente": len(lignes_facture_vente),
    "best_performers_ids": best_ids, "low_performers_ids": low_ids,
}
L.append("-- Counts: " + json.dumps(counts))

OUT_DIR = Path(__file__).resolve().parent
(OUT_DIR/"testentreprise17_seed.sql").write_text("\n".join(L) + "\n", encoding="utf-8")

json_data["summary"] = counts
(OUT_DIR/"testentreprise17_seed.json").write_text(
    json.dumps(json_data, indent=2, default=str), encoding="utf-8"
)

print("OK")
print(json.dumps(counts, indent=2))
