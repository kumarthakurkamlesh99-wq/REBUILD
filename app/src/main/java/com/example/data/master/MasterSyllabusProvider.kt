package com.example.data.master

data class MasterUnitData(
    val unitNumber: Int,
    val unitTitle: String,
    val description: String,
    val chapters: List<MasterChapterData>
)

data class MasterChapterData(
    val chapterNumber: Int,
    val title: String,
    val description: String,
    val topics: List<String>
)

data class MasterSubjectData(
    val code: String, // "PHYSICS", "CHEMISTRY", "BIOLOGY", "HINDI", "ENGLISH"
    val name: String,
    val iconName: String,
    val colorHex: String,
    val units: List<MasterUnitData>
)

object MasterSyllabusProvider {

    fun getAllMasterSubjects(): List<MasterSubjectData> {
        return listOf(
            getPhysicsSyllabus(),
            getChemistrySyllabus(),
            getBiologySyllabus(),
            getHindiSyllabus(),
            getEnglishSyllabus()
        )
    }

    fun getPhysicsSyllabus(): MasterSubjectData {
        return MasterSubjectData(
            code = "PHYSICS",
            name = "Physics (Class XII)",
            iconName = "bolt",
            colorHex = "#38E1FF",
            units = listOf(
                MasterUnitData(
                    unitNumber = 1,
                    unitTitle = "Unit I: Electrostatics",
                    description = "Electric charges, field lines, Gauss's Theorem, potential & capacitance",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 1,
                            title = "Electric Charges and Fields",
                            description = "Coulomb's Law, forces between charges, electric field & dipole, Gauss's law applications",
                            topics = listOf(
                                "Electric charges and conservation principle",
                                "Coulomb's Law and forces between multiple charges",
                                "Superposition principle and continuous charge distribution",
                                "Electric field, field lines and electric dipole",
                                "Torque on a dipole in a uniform electric field",
                                "Electric flux and Gauss's Theorem",
                                "Gauss's Law: Infinitely long straight wire",
                                "Gauss's Law: Uniformly charged infinite plane sheet",
                                "Gauss's Law: Uniformly charged thin spherical shell"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 2,
                            title = "Electrostatic Potential and Capacitance",
                            description = "Potential difference, equipotential surfaces, capacitors, dielectrics",
                            topics = listOf(
                                "Electric potential and potential difference",
                                "Electric potential due to a point charge and electric dipole",
                                "Equipotential surfaces and electrostatic potential energy",
                                "Conductors, insulators, free and bound charges",
                                "Dielectrics and electric polarization",
                                "Capacitors and capacitance in series & parallel",
                                "Energy stored in a capacitor",
                                "Van de Graaff generator principles"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 2,
                    unitTitle = "Unit II: Current Electricity",
                    description = "Ohm's law, resistivity, Kirchhoff's laws, Wheatstone & Potentiometer",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 3,
                            title = "Current Electricity & Circuits",
                            description = "Drift velocity, internal resistance, cells in combination, bridge circuits",
                            topics = listOf(
                                "Electric current, drift velocity and mobility",
                                "Ohm's law, electrical resistance and V-I characteristics",
                                "Electrical resistivity, conductivity and resistor color codes",
                                "Series and parallel combinations of resistors",
                                "Temperature dependence of resistance",
                                "Internal resistance of a cell, EMF and terminal potential difference",
                                "Combinations of cells in series and parallel",
                                "Kirchhoff's laws and network applications",
                                "Wheatstone bridge and Metre bridge experiments",
                                "Potentiometer: EMF comparison & internal resistance measurement"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 3,
                    unitTitle = "Unit III: Magnetic Effects of Current & Magnetism",
                    description = "Biot-Savart Law, Ampere's Law, Moving Coil Galvanometer, Earth magnetism",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 4,
                            title = "Moving Charges and Magnetism",
                            description = "Magnetic forces, Cyclotron, galvanometer conversion",
                            topics = listOf(
                                "Biot-Savart Law and circular loop application",
                                "Ampere's Circuital Law and solenoid applications",
                                "Force on a moving charge in magnetic and electric fields",
                                "Cyclotron principle, construction and working",
                                "Force on a current-carrying conductor in a magnetic field",
                                "Force between two parallel current-carrying conductors & Ampere definition",
                                "Torque on a current loop in a magnetic field",
                                "Moving Coil Galvanometer: Conversion to Ammeter and Voltmeter"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 5,
                            title = "Magnetism and Matter",
                            description = "Magnetic dipoles, Earth's magnetic elements, Dia/Para/Ferromagnetism",
                            topics = listOf(
                                "Current loop as a magnetic dipole and electron magnetic moment",
                                "Earth's magnetic field and magnetic elements (Declination, Dip, Horizontal component)",
                                "Diamagnetic, Paramagnetic, and Ferromagnetic substances",
                                "Electromagnets, permanent magnets and hysteresis"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 4,
                    unitTitle = "Unit IV: Electromagnetic Induction & Alternating Currents",
                    description = "Faraday's laws, Lenz's law, LCR circuits, resonance, AC generator, transformer",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 6,
                            title = "Electromagnetic Induction",
                            description = "Induced EMF, Eddy currents, self and mutual inductance",
                            topics = listOf(
                                "Faraday's laws of electromagnetic induction",
                                "Lenz's Law and conservation of energy",
                                "Eddy currents and practical damping applications",
                                "Self-induction and Mutual induction coefficients",
                                "Displacement current concept and Maxwell's correction"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 7,
                            title = "Alternating Currents",
                            description = "Peak & RMS values, LCR resonance, power in AC, transformers",
                            topics = listOf(
                                "Alternating currents: Peak and RMS values of AC and voltage",
                                "Reactance and Impedance in inductive and capacitive circuits",
                                "LC oscillations and energy exchange",
                                "LCR series circuit analysis and resonance frequency",
                                "Power in AC circuits and wattless current",
                                "AC generator principles and working",
                                "Transformer: Step-up and Step-down efficiency and losses"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 5,
                    unitTitle = "Unit V: Electromagnetic Waves",
                    description = "Transverse EM waves and the complete electromagnetic spectrum",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 8,
                            title = "Electromagnetic Waves",
                            description = "Characteristics, wave propagation, EM spectrum uses",
                            topics = listOf(
                                "Characteristics and transverse nature of EM waves",
                                "Electromagnetic spectrum: Radio waves and Microwaves",
                                "Electromagnetic spectrum: Infrared and Visible rays",
                                "Electromagnetic spectrum: Ultraviolet, X-rays, and Gamma rays",
                                "Basic applications of each band in communication and medicine"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 6,
                    unitTitle = "Unit VI: Optics",
                    description = "Ray Optics, Optical Instruments, Wave Optics, Huygens, Interference & Diffraction",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 9,
                            title = "Ray Optics and Optical Instruments",
                            description = "Reflection, spherical surfaces, lenses, prism, eye, telescope, microscope",
                            topics = listOf(
                                "Reflection of light, spherical mirrors and mirror formula",
                                "Refraction, total internal reflection and optical fibers",
                                "Refraction at spherical surfaces and thin lens formula",
                                "Lens Maker's formula, magnification and power of a lens",
                                "Combination of thin lenses in contact",
                                "Refraction and dispersion through a glass prism",
                                "Scattering of light (blue sky and reddish sunset)",
                                "Human eye: Defects of vision (Myopia, Hypermetropia) and correction",
                                "Compound microscope construction and magnifying power",
                                "Astronomical telescope (Refracting and Reflecting type)"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 10,
                            title = "Wave Optics",
                            description = "Huygens' wave theory, Young's double slit, diffraction, polaroids",
                            topics = listOf(
                                "Wavefront and Huygens' principle",
                                "Reflection and refraction of plane waves using wavefronts",
                                "Interference of light and Young's double-slit experiment",
                                "Diffraction due to a single slit and central maxima width",
                                "Resolving power of microscope and astronomical telescope",
                                "Polarization of light, Brewster's law, and Polaroids"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 7,
                    unitTitle = "Unit VII: Dual Nature of Matter & Radiation",
                    description = "Photoelectric effect, Einstein's equation, de-Broglie wavelength",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 11,
                            title = "Dual Nature of Radiation and Matter",
                            description = "Hertz/Lenard observations, work function, Davisson-Germer",
                            topics = listOf(
                                "Photoelectric effect: Hertz and Lenard's observations",
                                "Einstein's photoelectric equation and particle nature of light",
                                "Matter waves: de-Broglie relation and wavelength of electrons",
                                "Davisson-Germer experiment and electron diffraction verification"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 8,
                    unitTitle = "Unit VIII: Atoms and Nuclei",
                    description = "Rutherford & Bohr models, radioactivity, nuclear binding energy, fission & fusion",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 12,
                            title = "Atoms",
                            description = "Alpha particle scattering, energy levels, Bohr hydrogen spectrum",
                            topics = listOf(
                                "Alpha-particle scattering experiment and distance of closest approach",
                                "Rutherford's atomic model and its limitations",
                                "Bohr's model of hydrogen atom and quantized energy levels",
                                "Hydrogen emission spectrum (Lyman, Balmer, Paschen series)"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 13,
                            title = "Nuclei",
                            description = "Nuclear size, isotopes, radioactivity decay law, nuclear energy",
                            topics = listOf(
                                "Composition and size of nucleus, atomic masses, isotopes, isobars, isotones",
                                "Radioactivity (alpha, beta, gamma rays and properties)",
                                "Radioactive decay law and half-life calculations",
                                "Mass-energy relation and mass defect",
                                "Binding energy per nucleon and stability curve",
                                "Nuclear fission, chain reaction, and nuclear fusion (stellar energy)"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 9,
                    unitTitle = "Unit IX: Electronic Devices",
                    description = "Semiconductors, p-n junction, Zener diode, Transistors, Logic Gates",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 14,
                            title = "Semiconductor Electronics: Materials, Devices & Simple Circuits",
                            description = "Diodes, rectifiers, transistors, CE amplifier, logic gates",
                            topics = listOf(
                                "Semiconductor materials: Intrinsic and extrinsic (n-type, p-type)",
                                "p-n junction diode formation and I-V characteristics (Forward/Reverse)",
                                "Zener diode as a voltage regulator",
                                "Optoelectronic devices: Photodiode, LED, and Solar cell",
                                "Junction transistor: Action and characteristics in Common Emitter (CE)",
                                "Transistor as a switch, amplifier, and oscillator",
                                "Logic Gates (OR, AND, NOT, NAND, NOR) and truth tables",
                                "Elementary Boolean algebra and De Morgan's theorems"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 10,
                    unitTitle = "Unit X: Communication Systems",
                    description = "Communication block diagram, EM wave propagation, modulation, satellite/mobile",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 15,
                            title = "Principles of Communication Systems",
                            description = "Ground/sky/space waves, AM modulation, internet & remote sensing",
                            topics = listOf(
                                "Elements of a communication system (Block diagram)",
                                "Bandwidth of signals (Speech, TV, Digital) and transmission medium",
                                "Propagation of EM waves: Ground, sky, and space wave propagation",
                                "Modulation: Need for modulation, Amplitude Modulation (AM) generation & detection",
                                "Basics of satellite communication, mobile cellular network, internet, and remote sensing"
                            )
                        )
                    )
                )
            )
        )
    }

    fun getChemistrySyllabus(): MasterSubjectData {
        return MasterSubjectData(
            code = "CHEMISTRY",
            name = "Chemistry (Class XII)",
            iconName = "science",
            colorHex = "#FFB300",
            units = listOf(
                MasterUnitData(
                    unitNumber = 1,
                    unitTitle = "Unit I: Physical Chemistry",
                    description = "Solid State, Solutions, Electrochemistry, Chemical Kinetics, Surface Chemistry",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 1,
                            title = "The Solid State",
                            description = "Crystal lattices, unit cells, packing efficiency, voids, defects, magnetic properties",
                            topics = listOf(
                                "Classification of solids: Crystalline vs Amorphous",
                                "Crystal lattices and unit cells (Primitive, BCC, FCC)",
                                "Number of atoms per unit cell and packing efficiency calculations",
                                "Calculation of density of unit cells and interstitial voids (Tetrahedral/Octahedral)",
                                "Point defects in crystals (Stoichiometric, Frenkel, Schottky, Non-stoichiometric)",
                                "Electrical properties (Conductors, Insulators, Semiconductors) and Magnetic properties"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 2,
                            title = "Solutions",
                            description = "Concentration terms, Raoult's law, colligative properties, Van 't Hoff factor",
                            topics = listOf(
                                "Concentration terms: Molarity, Molality, Mole fraction, ppm",
                                "Solubility of gases in liquids and Henry's Law",
                                "Raoult's Law for volatile and non-volatile solutes (Ideal & Non-ideal solutions)",
                                "Colligative property 1: Relative lowering of vapour pressure",
                                "Colligative property 2: Elevation of boiling point (Ebuliometry)",
                                "Colligative property 3: Depression of freezing point (Cryometry)",
                                "Colligative property 4: Osmotic pressure and reverse osmosis",
                                "Abnormal molecular mass and Van 't Hoff factor (i)"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 3,
                            title = "Electrochemistry",
                            description = "Redox, Kohlrausch's law, Nernst equation, corrosion, commercial batteries",
                            topics = listOf(
                                "Redox reactions and galvanic electrochemical cells",
                                "Electrolytic conductance, specific, molar and equivalent conductivity",
                                "Kohlrausch's Law of independent migration of ions and applications",
                                "Standard Electrode Potential (EMF of a cell) and Electrochemical series",
                                "Nernst equation for cell potential and equilibrium constant calculation",
                                "Gibbs free energy change and electrical work",
                                "Dry cell, Lead-acid battery, Nickel-Cadmium and Fuel cells",
                                "Corrosion mechanism, rusting of iron and rust prevention"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 4,
                            title = "Chemical Kinetics",
                            description = "Rate of reaction, rate law, integrated rate equations, half-life, Arrhenius",
                            topics = listOf(
                                "Rate of reaction (Average and instantaneous) and factors affecting rate",
                                "Rate law expression, Rate constant (k), Order and Molecularity of reaction",
                                "Integrated rate equations for Zero order and First order reactions",
                                "Half-life of a reaction and numerical problem solving",
                                "Arrhenius equation, temperature dependence, Activation Energy (Ea)",
                                "Collision theory of chemical reactions"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 5,
                            title = "Surface Chemistry",
                            description = "Adsorption, catalysis, colloids, emulsions, colloidal properties",
                            topics = listOf(
                                "Adsorption: Physisorption vs Chemisorption and Freundlich isotherm",
                                "Catalysis: Homogeneous, Heterogeneous, Enzyme catalysis and shape selectivity",
                                "Colloids: Lyophilic vs Lyophobic, Multi-molecular, Macro-molecular, Associated (Micelles)",
                                "Properties of colloidal solutions: Tyndall effect, Brownian movement, Electrophoresis",
                                "Coagulation, Hardy-Schulze rule, and protective colloids (Gold number)",
                                "Emulsions and types (O/W, W/O) and applications"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 2,
                    unitTitle = "Unit II: Inorganic Chemistry",
                    description = "Isolation of elements, p-Block, d- and f-Block, Coordination Compounds",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 6,
                            title = "General Principles and Processes of Isolation of Elements",
                            description = "Extraction methods, concentration, roasting/calcination, refining",
                            topics = listOf(
                                "Occurrence of metals, minerals and ores",
                                "Concentration methods: Gravity separation, Magnetic, Froth flotation, Leaching",
                                "Thermodynamic principles of metallurgy (Ellingham diagram)",
                                "Extraction of crude metal: Calcination, Roasting, Reduction (Smelting)",
                                "Electrochemical principles and extraction of Aluminium (Hall-Héroult process)",
                                "Refining of metals: Liquation, Distillation, Electrolytic, Zone refining, Vapour phase"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 7,
                            title = "p-Block Elements",
                            description = "Group 15, 16, 17, 18 elements, trends, compounds of nitrogen, phosphorus, halogens",
                            topics = listOf(
                                "Group 15: Nitrogen family (Dinitrogen, Ammonia, Nitric acid, Phosphorus allotropes & halides)",
                                "Group 16: Oxygen family (Dioxygen, Ozone, Sulphur allotropes, Sulphur dioxide, Sulphuric acid)",
                                "Group 17: Halogen family (Chlorine, Hydrochloric acid, Interhalogen compounds, Oxoacids)",
                                "Group 18: Noble gases (Electronic configuration, Xenon fluorides and oxofluorides structures)"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 8,
                            title = "d- and f-Block Elements",
                            description = "Transition elements, Lanthanoids, Actinoids, Lanthanoid contraction, K2Cr2O7, KMnO4",
                            topics = listOf(
                                "General trends in properties of 3d transition elements (Metallic, Ionic radii, Oxidation states)",
                                "Catalytic properties, magnetic properties, coloured ions, complex formation, interstitial compounds",
                                "Preparation, properties, and oxidizing action of K2Cr2O7 (Potassium dichromate)",
                                "Preparation, properties, and oxidizing action of KMnO4 (Potassium permanganate)",
                                "Lanthanoids: Electronic configuration, oxidation states, and Lanthanoid contraction consequences",
                                "Actinoids: General characteristics and comparison with Lanthanoids"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 9,
                            title = "Coordination Compounds",
                            description = "Werner's theory, ligands, IUPAC naming, isomerism, VBT, CFT",
                            topics = listOf(
                                "Werner's coordination theory, coordination entity, central atom, and ligands",
                                "Coordination number, coordination sphere, oxidation number and IUPAC nomenclature",
                                "Isomerism in coordination compounds: Structural (Ionization, Hydrate, Linkage, Coordination)",
                                "Stereoisomerism: Geometrical (cis/trans) and Optical isomerism",
                                "Valence Bond Theory (VBT): Inner and outer orbital complexes, magnetic behavior",
                                "Crystal Field Theory (CFT): d-orbital splitting in octahedral and tetrahedral complexes",
                                "Spectrochemical series, colour in coordination compounds, and biological applications"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 3,
                    unitTitle = "Unit III: Organic Chemistry",
                    description = "Haloalkanes, Alcohols, Carbonyls, Amines, Biomolecules, Polymers, Everyday Chemistry",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 10,
                            title = "Haloalkanes and Haloarenes",
                            description = "Nomenclature, C-X bond, SN1/SN2 mechanisms, optical rotation, polyhalogen",
                            topics = listOf(
                                "Nomenclature, nature of C-X bond, physical properties",
                                "Methods of preparation from alcohols, hydrocarbons, and halogen exchange",
                                "Nucleophilic substitution mechanisms: SN1 vs SN2 (Kinetics, stereochemistry, reactivity)",
                                "Elimination reactions (Saytzeff rule) and reaction with metals (Grignard reagent)",
                                "Haloarenes: Low reactivity towards nucleophilic substitution and electrophilic substitution",
                                "Polyhalogen compounds: Chloroform, Iodoform, Freons, DDT"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 11,
                            title = "Alcohols, Phenols and Ethers",
                            description = "Preparation, properties, dehydration, Kolbe's, Reimer-Tiemann, Williamson",
                            topics = listOf(
                                "Alcohols: Classification, nomenclature, preparation from alkenes, carbonyls, Grignard",
                                "Physical properties, acidity of alcohols, Lucas test to distinguish 1°, 2°, 3° alcohols",
                                "Chemical reactions: Esterification, dehydration mechanism, oxidation",
                                "Phenols: Preparation from cumene, diazonium salts, chlorobenzene (Dow process)",
                                "Acidity of phenols, electrophilic substitution (Nitration, Halogenation)",
                                "Kolbe's reaction, Reimer-Tiemann reaction, reaction with Zinc dust and oxidation",
                                "Ethers: Williamson's synthesis and cleavage of C-O bond with HI"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 12,
                            title = "Aldehydes, Ketones and Carboxylic Acids",
                            description = "Carbonyl reactions, Aldol, Cannizzaro, Rosenmund, Clemmensen, acidity of acids",
                            topics = listOf(
                                "Nomenclature, structure of carbonyl group, preparation from alcohols, alkenes, alkynes",
                                "Rosenmund reduction, Stephen reaction, Etard reaction, Gattermann-Koch reaction",
                                "Nucleophilic addition reactions: Addition of HCN, NaHSO3, Alcohols, Grignard, Ammonia derivatives",
                                "Reduction: Clemmensen and Wolff-Kishner reductions",
                                "Oxidation: Tollens' test, Fehling's test, Haloform reaction (Iodoform test)",
                                "Reactions due to alpha-hydrogen: Aldol condensation and Cross aldol condensation",
                                "Cannizzaro reaction and Electrophilic substitution of benzaldehyde",
                                "Carboxylic Acids: Acidity, effect of substituents, preparation, HVZ reaction, decarboxylation"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 13,
                            title = "Amines (Nitrogen Containing Compounds)",
                            description = "Basicity of amines, Hoffmann bromamide, Carbylamine, Diazonium salts",
                            topics = listOf(
                                "Classification, nomenclature, and structure of amines",
                                "Preparation: Reduction of nitro compounds, nitriles, amides; Gabriel phthalimide, Hoffmann bromamide",
                                "Physical properties and basicity of amines (Gas phase vs Aqueous solution)",
                                "Chemical reactions: Carbylamine test, reaction with nitrous acid, Hinsberg's reagent test",
                                "Electrophilic substitution of aniline: Bromination, Nitration, Sulphonation",
                                "Diazonium Salts: Preparation, stability, Sandmeyer reaction, Gattermann, Coupling reactions"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 14,
                            title = "Biomolecules",
                            description = "Carbohydrates, amino acids, protein structure, nucleic acids, vitamins, enzymes",
                            topics = listOf(
                                "Carbohydrates: Monosaccharides (Glucose and Fructose open & ring structures, mutarotation)",
                                "Disaccharides (Sucrose, Lactose, Maltose with glycosidic linkage) and Polysaccharides (Starch, Cellulose, Glycogen)",
                                "Proteins: Amino acids (Zwitter ion, essential vs non-essential), peptide linkage",
                                "Primary, Secondary (alpha-helix, beta-pleated sheet), Tertiary, and Quaternary protein structures",
                                "Denaturation of proteins and enzymes (Coenzymes, specificity)",
                                "Nucleic Acids: DNA and RNA composition, double helix structure, replication, transcription, translation",
                                "Vitamins: Classification (Fat soluble vs Water soluble) and deficiency diseases"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 15,
                            title = "Polymers",
                            description = "Addition & condensation polymerization, Bakelite, Nylon, Rubber, biodegradable",
                            topics = listOf(
                                "Classification of polymers: Natural, Semi-synthetic, Synthetic; Elastomers, Fibres, Thermoplastics, Thermosetting",
                                "Types of polymerization: Addition (Free radical mechanism) and Condensation",
                                "Important polymers: Polythene (LDPE, HDPE), Teflon, PAN, Nylon-6,6, Nylon-6, Dacron (Terylene)",
                                "Bakelite, Melamine-formaldehyde, Urea-formaldehyde resins",
                                "Natural rubber, Vulcanization of rubber, and synthetic rubbers (Buna-S, Buna-N, Neoprene)",
                                "Biodegradable polymers: PHBV, Nylon-2-nylon-6"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 16,
                            title = "Chemistry in Everyday Life",
                            description = "Drugs & medicines, food additives, artificial sweeteners, soaps & detergents",
                            topics = listOf(
                                "Drugs and classification: Analgesics (Narcotic, Non-narcotic), Antipyretics, Tranquilizers, Antimicrobials",
                                "Antibiotics (Bactericidal vs Bacteriostatic, Broad spectrum), Antiseptics and Disinfectants, Antacids, Antihistamines",
                                "Chemicals in food: Artificial sweetening agents (Aspartame, Saccharin, Sucralose, Alitame)",
                                "Food preservatives (Sodium benzoate, salts of sorbic acid) and Antioxidants",
                                "Cleansing agents: Soaps (Saponification), synthetic detergents (Anionic, Cationic, Non-ionic)"
                            )
                        )
                    )
                )
            )
        )
    }

    fun getBiologySyllabus(): MasterSubjectData {
        return MasterSubjectData(
            code = "BIOLOGY",
            name = "Biology (Class XII)",
            iconName = "eco",
            colorHex = "#00E676",
            units = listOf(
                MasterUnitData(
                    unitNumber = 1,
                    unitTitle = "Unit I: Reproduction",
                    description = "Reproduction in organisms, flowering plants, human reproduction, reproductive health",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 1,
                            title = "Reproduction in Organisms",
                            description = "Asexual and sexual modes of reproduction across organisms",
                            topics = listOf(
                                "Asexual reproduction: Binary fission, budding, sporulation, vegetative propagation",
                                "Sexual reproduction: Pre-fertilization, fertilization, and post-fertilization events"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 2,
                            title = "Sexual Reproduction in Flowering Plants",
                            description = "Flower structure, micro/megasporogenesis, pollination, double fertilization, seeds",
                            topics = listOf(
                                "Flower structure, development of male and female gametophytes",
                                "Pollination: Types (Autogamy, Geitonogamy, Xenogamy), agencies, outbreeding devices",
                                "Pollen-pistil interaction, double fertilization, endosperm and embryo development",
                                "Seed development, fruit formation, apomixis, and polyembryony"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 3,
                            title = "Human Reproduction",
                            description = "Male/female reproductive systems, gametogenesis, menstrual cycle, pregnancy, lactation",
                            topics = listOf(
                                "Male reproductive system: Testes structure, seminiferous tubules, accessory ducts & glands",
                                "Female reproductive system: Ovaries, fallopian tubes, uterus, mammary glands",
                                "Gametogenesis: Spermatogenesis and Oogenesis, hormonal regulation",
                                "Menstrual cycle and hormonal control (FSH, LH, Estrogen, Progesterone)",
                                "Fertilization, blastocyst formation, implantation, placenta, pregnancy, parturition, lactation"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 4,
                            title = "Reproductive Health",
                            description = "Contraception, STI prevention, infertility, assisted technologies (IVF, ZIFT, GIFT)",
                            topics = listOf(
                                "Need for reproductive health and population explosion",
                                "Contraceptive methods: Natural, Barrier, IUDs, Oral pills, Surgical (Vasectomy, Tubectomy)",
                                "Medical Termination of Pregnancy (MTP) and Sexually Transmitted Infections (STIs)",
                                "Infertility causes and Assisted Reproductive Technologies (ART): IVF, ET, ZIFT, GIFT, ICSI, IUI"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 2,
                    unitTitle = "Unit II: Genetics and Evolution",
                    description = "Mendelian genetics, molecular basis of inheritance, DNA fingerprinting, human evolution",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 5,
                            title = "Principles of Inheritance and Variation",
                            description = "Mendel's laws, sex determination, linkage, pedigree, chromosomal disorders",
                            topics = listOf(
                                "Mendelian principles of inheritance: Monohybrid and Dihybrid crosses, Dominance, Segregation",
                                "Deviations from Mendelism: Incomplete dominance, Co-dominance, Multiple alleles (Blood groups), Pleiotropy",
                                "Chromosomal theory of inheritance, Linkage and Recombination (Morgan's fruit fly experiments)",
                                "Sex determination in humans, birds, honey bees; Sex-linked inheritance (Haemophilia, Colour blindness)",
                                "Mendelian disorders: Thalassemia, Sickle-cell anaemia, Phenylketonuria",
                                "Chromosomal disorders: Down's syndrome, Turner's syndrome, Klinefelter's syndrome"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 6,
                            title = "Molecular Basis of Inheritance",
                            description = "DNA structure, replication, transcription, translation, genetic code, HGP, fingerprinting",
                            topics = listOf(
                                "DNA as genetic material (Griffith, Avery-MacLeod-McCarty, Hershey-Chase experiments)",
                                "DNA structure (Watson-Crick model), packaging of DNA helix (Nucleosomes, Chromatin)",
                                "DNA replication (Semi-conservative mechanism, Meselson-Stahl experiment)",
                                "Transcription: Transcription unit, RNA types, processing of hnRNA (Splicing, Capping, Tailing)",
                                "Genetic code features (Triplet, degenerate, universal) and tRNA adaptor molecule",
                                "Translation (Protein synthesis): Initiation, elongation, termination, peptide bond formation",
                                "Regulation of gene expression: Operon model (lac operon in E. coli)",
                                "Human Genome Project (HGP) goals and DNA fingerprinting technique & applications"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 7,
                            title = "Evolution",
                            description = "Origin of life, Darwinian theory, evidence of evolution, Hardy-Weinberg, human lineage",
                            topics = listOf(
                                "Origin of life (Oparin-Haldane hypothesis, Miller-Urey experiment)",
                                "Evidence for biological evolution: Paleontological, Comparative anatomy (Homologous & Analogous)",
                                "Embryological evidence, Biochemical evidence, Natural selection (Industrial melanism)",
                                "Darwin's theory, Lamarckism, Mutation theory of Hugo de Vries",
                                "Modern synthetic theory of evolution, Hardy-Weinberg principle and factors affecting equilibrium",
                                "Adaptive radiation, speciation, and evolutionary stages of Homo sapiens"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 3,
                    unitTitle = "Unit III: Biology in Human Welfare",
                    description = "Human health & diseases, immunology, food production strategies, microbes in welfare",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 8,
                            title = "Human Health and Diseases",
                            description = "Pathogens, immunity, vaccines, HIV/AIDS, cancer, substance abuse",
                            topics = listOf(
                                "Pathogens causing infectious diseases: Malaria (Plasmodium life cycle), Typhoid, Pneumonia, Amoebiasis, Ringworm",
                                "Immunity: Innate (Physical, Physiological, Cellular, Cytokine) vs Acquired immunity",
                                "Humoral vs Cell-mediated immunity, Active and Passive immunity, Vaccines and immunization",
                                "Allergies, Autoimmunity (Rheumatoid arthritis), Immune system in body (Lymphoid organs)",
                                "HIV / AIDS: Transmission, retrovirus replication cycle, symptoms, prevention, ELISA test",
                                "Cancer: Types, causes (Carcinogens, Oncogenes), diagnosis, treatment (Chemo, Radio, Immuno)",
                                "Adolescence and drug/alcohol abuse: Opioids, Cannabinoids, Coca alkaloids, addiction, prevention"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 9,
                            title = "Strategies for Enhancement in Food Production",
                            description = "Animal husbandry, plant breeding, single-cell protein, tissue culture",
                            topics = listOf(
                                "Animal husbandry: Dairy, poultry, apiculture (beekeeping), fisheries, MOET technique",
                                "Plant breeding for disease resistance, pest resistance, and improved nutritional quality (Biofortification)",
                                "Single Cell Protein (SCP) production (Spirulina, Methylophilus methylotrophus)",
                                "Plant tissue culture: Totipotency, explants, micropropagation, somaclones, somatic hybridization"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 10,
                            title = "Microbes in Human Welfare",
                            description = "Household, industrial, sewage treatment, biogas, biocontrol, biofertilizers",
                            topics = listOf(
                                "Microbes in household food processing: Curd (LAB), bread dough, cheese, Toddy",
                                "Microbes in industrial products: Fermented beverages, Antibiotics (Penicillin), Organic acids, Enzymes, Statins",
                                "Microbes in sewage treatment: Primary treatment, Secondary (biological) treatment, BOD, Activated sludge",
                                "Microbes in biogas production (Methanogens, anaerobic digestion)",
                                "Microbes as biocontrol agents (Bacillus thuringiensis, Trichoderma, Baculoviruses)",
                                "Microbes as biofertilizers (Rhizobium, Azospirillum, Azotobacter, Mycorrhiza, Cyanobacteria)"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 4,
                    unitTitle = "Unit IV: Biotechnology and Its Applications",
                    description = "Genetic engineering, recombinant DNA technology, Bt crops, insulin, gene therapy",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 11,
                            title = "Biotechnology: Principles and Processes",
                            description = "Restriction enzymes, cloning vectors, PCR, bioreactors, downstream processing",
                            topics = listOf(
                                "Principles of biotechnology: Genetic engineering and maintenance of sterile ambience",
                                "Tools of recombinant DNA technology: Restriction endonucleases, DNA ligase, DNA polymerase",
                                "Cloning vectors: Plasmids (pBR322 characteristics), selectable markers, cloning sites",
                                "Competent host transformation techniques (Heat shock, Micro-injection, Biolistics / Gene gun)",
                                "Processes of recombinant DNA technology: DNA isolation, Gel electrophoresis, PCR amplification",
                                "Bioreactors (Stirred-tank) and Downstream processing of biotechnology products"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 12,
                            title = "Biotechnology and Its Applications",
                            description = "Applications in agriculture (Bt cotton, RNAi), medicine (Insulin, Gene therapy), biosafety",
                            topics = listOf(
                                "Biotechnological applications in agriculture: Bt crops (Bt cotton mechanism), Pest resistant plants (RNA interference)",
                                "Biotechnological applications in medicine: Genetically engineered insulin (Humulin), Gene therapy (ADA deficiency)",
                                "Molecular diagnosis: PCR, ELISA, Antigen-antibody assays",
                                "Transgenic animals: Models for human diseases, biological products (alpha-1-antitrypsin, Rosie cow)",
                                "Ethical issues, GEAC committee, Biopiracy (Basmati rice patent controversies), and Biosafety"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 5,
                    unitTitle = "Unit V: Ecology and Environment",
                    description = "Organisms & populations, ecosystem structure, biodiversity conservation, environmental issues",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 13,
                            title = "Organisms and Populations",
                            description = "Abiotic adaptations, population growth models, population interactions",
                            topics = listOf(
                                "Organism and its environment: Responses to abiotic factors (Regulate, Conform, Migrate, Suspend)",
                                "Adaptations: Morphological, physiological, behavioral adaptations in desert plants and animals",
                                "Population attributes: Birth rate, death rate, sex ratio, age pyramids",
                                "Population growth models: Exponential growth vs Logistic growth (Carrying capacity K)",
                                "Population interactions: Mutualism, Competition (Gause's principle), Predation, Parasitism, Commensalism, Amensalism"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 14,
                            title = "Ecosystem",
                            description = "Structure, productivity, decomposition, energy flow, ecological pyramids, nutrient cycles",
                            topics = listOf(
                                "Ecosystem structure and functions: Primary productivity (GPP, NPP) and Secondary productivity",
                                "Decomposition process: Fragmentation, Leaching, Catabolism, Humification, Mineralization",
                                "Energy flow: Food chain (Grazing, Detritus), Food web, 10% energy transfer law",
                                "Ecological pyramids: Pyramid of number, biomass, and energy (Upright vs Inverted)",
                                "Ecological succession: Hydrarch and Xerarch succession, pioneer and climax communities",
                                "Nutrient cycling (Biogeochemical cycles): Carbon cycle, Phosphorus cycle, and Ecosystem services"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 15,
                            title = "Biodiversity and Conservation",
                            description = "Biodiversity patterns, loss of biodiversity, In-situ and Ex-situ conservation",
                            topics = listOf(
                                "Levels of biodiversity: Genetic, Species, Ecological diversity; Global species diversity estimates",
                                "Patterns of biodiversity: Latitudinal gradients, Species-Area relationship (Alexander von Humboldt)",
                                "Importance of biodiversity and Causes of biodiversity losses (The Evil Quartet)",
                                "Biodiversity conservation: In-situ (National parks, Sanctuaries, Biosphere reserves, Sacred groves)",
                                "Ex-situ conservation (Zoological parks, Botanical gardens, Cryopreservation, Seed banks)"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 16,
                            title = "Environmental Issues",
                            description = "Air/water pollution, solid waste management, greenhouse effect, ozone depletion",
                            topics = listOf(
                                "Air pollution and control: Electrostatic precipitators, scrubbers, catalytic converters",
                                "Water pollution: Biochemical Oxygen Demand (BOD), Biomagnification (DDT), Eutrophication",
                                "Solid waste management: Sanitary landfills, e-waste recycling, plastic waste remediation",
                                "Greenhouse effect and Global warming causes and countermeasures",
                                "Ozone depletion in stratosphere: Chlorofluorocarbons (CFCs), Montreal Protocol",
                                "Deforestation causes, Jhum cultivation, Reforestation, Chipko Movement, Joint Forest Management"
                            )
                        )
                    )
                )
            )
        )
    }

    fun getHindiSyllabus(): MasterSubjectData {
        return MasterSubjectData(
            code = "HINDI",
            name = "Hindi (Class XII - Digant Part-2)",
            iconName = "menu_book",
            colorHex = "#FF5722",
            units = listOf(
                MasterUnitData(
                    unitNumber = 1,
                    unitTitle = "Unit I: Literature - Prose (गद्य खण्ड)",
                    description = "Digant Part-2: 15 Chapters of Prose by prominent Hindi essayists & novelists",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 1,
                            title = "बातचीत (बालकृष्ण भट्ट)",
                            description = "निबंध विधा: वाक्शक्ति का महत्व, बातचीत की कला और सामाजिक शिष्टाचार",
                            topics = listOf(
                                "पाठ का सारांश एवं बालकृष्ण भट्ट का जीवन परिचय",
                                "वाक्शक्ति का महत्व एवं बातचीत के विभिन्न प्रकार",
                                "एडिसन और बेन जॉनसन के विचारों का विश्लेषण",
                                "अभ्यास प्रश्नोत्तर एवं व्याख्या"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 2,
                            title = "उसने कहा था (चन्द्रधर शर्मा गुलेरी)",
                            description = "अमर प्रेम एवं कर्तव्यनिष्ठा की ऐतिहासिक कहानी: लहना सिंह का बलिदान",
                            topics = listOf(
                                "कहानी का कथानक एवं चरित्र-चित्रण (लहना सिंह, सूबेदारनी, वजीरा सिंह)",
                                "फ्लैशबैक तकनीक एवं प्रथम विश्वयुद्ध की पृष्ठभूमि",
                                "प्रेम, त्याग और कर्तव्यनिष्ठा के अंतर्संबंध",
                                "गद्यांश व्याख्या एवं परीक्षा उपयोगी प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 3,
                            title = "संपूर्ण क्रांति (जयप्रकाश नारायण)",
                            description = "ऐतिहासिक भाषण: पटना के गांधी मैदान का जन-आंदोलन एवं समग्र क्रांति दर्शन",
                            topics = listOf(
                                "जयप्रकाश नारायण का जीवन दर्शन एवं संपूर्ण क्रांति का संदेश",
                                "भ्रष्टाचार, बेरोजगारी और शिक्षा सुधार पर विचार",
                                "लोकतंत्र की वास्तविक परिभाषा एवं जन-समिति का महत्व",
                                "प्रमुख भाषण अंशों की व्याख्या एवं प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 4,
                            title = "अर्धनारीश्वर (रामधारी सिंह 'दिनकर')",
                            description = "स्त्री-पुरुष समानता, पौराणिक प्रतीक एवं दिनकर का मानवतावादी दृष्टिकोण",
                            topics = listOf(
                                "अर्धनारीश्वर की पौराणिक एवं दार्शनिक संकल्पना",
                                "स्त्री-पुरुष के समान अधिकार एवं समाज में संतुलन",
                                "गांधी जी एवं बुद्ध के विचारों का संदर्भ",
                                "गद्यांश की सप्रसंग व्याख्या"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 5,
                            title = "रोज़ (अज्ञेय - सच्चिदानंद हीरानंद वात्स्यायन)",
                            description = "मध्यवर्गीय गृहिणी मालती का नीरस, एकरस जीवन एवं आधुनिक अवसाद",
                            topics = listOf(
                                "कहानी का सारांश एवं मालती का चरित्र-चित्रण",
                                "मध्यवर्गीय दांपत्य जीवन की यांत्रिकता और ऊब",
                                "समय, वातावरण और मानवीय संवेदना का चित्रण",
                                "महत्वपूर्ण उद्धरण एवं प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 6,
                            title = "एक लेख और एक पत्र (भगत सिंह)",
                            description = "विद्यार्थी और राजनीति, आत्महत्या के विरुद्ध तर्क एवं देशभक्ति का संदेश",
                            topics = listOf(
                                "भगत सिंह का छात्रों और राजनीति पर दृष्टिकोण",
                                "सुखदेव के नाम पत्र: कायरता बनाम क्रांतिकारी कर्तव्य",
                                "साहस, बलिदान और देशभक्ति के आदर्श",
                                "महत्वपूर्ण प्रश्न एवं व्याख्या"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 7,
                            title = "ओ सदानीरा (जगदीश चंद्र माथुर)",
                            description = "गंडक नदी की ऐतिहासिक, सांस्कृतिक और प्राकृतिक यात्रा का ललित निबंध",
                            topics = listOf(
                                "चंपारण का इतिहास, बौद्ध संस्कृति और गंडक नदी",
                                "गांधी जी का चंपारण सत्याग्रह एवं आश्रम विद्यालय",
                                "पर्यावरण विनाश और बाढ़ की विभीषिका",
                                "ललित निबंध की विशेषताएं एवं प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 8,
                            title = "सिपाही की माँ (मोहन राकेश)",
                            description = "एकांकी: युद्ध की त्रासदी, माँ बिशनी और बहन मुन्नी की मार्मिक प्रतीक्षा",
                            topics = listOf(
                                "एकांकी का कथानक एवं बिशनी का वात्सल्य",
                                "द्वितीय विश्वयुद्ध की बर्मी त्रासदी एवं गरीबी का प्रभाव",
                                "पात्रों का यथार्थवादी संवाद एवं चरित्र",
                                "एकांकी के प्रमुख प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 9,
                            title = "प्रगीत और समाज (नामवर सिंह)",
                            description = "आलोचनात्मक निबंध: आधुनिक कविता में आत्मपरक प्रगीत एवं सामाजिक चेतना",
                            topics = listOf(
                                "प्रगीत काव्य की परिभाषा और ऐतिहासिक विकास",
                                "मुक्तिबोध, त्रिलोचन और शमशेर की कविताओं का विश्लेषण",
                                "व्यक्तिगत संवेदना बनाम सामाजिक यथार्थ",
                                "आलोचनात्मक प्रश्नों के उत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 10,
                            title = "जूठन (ओमप्रकाश वाल्मीकि)",
                            description = "दलित आत्मकथा: सामाजिक भेदभाव, जातीय उत्पीड़न एवं आत्मसम्मान का संघर्ष",
                            topics = listOf(
                                "आत्मकथा का मर्मस्पर्शी यथार्थ एवं विद्यालयीय प्रताड़ना",
                                "जूठन खाने की विवशता और प्रतिरोध की चेतना",
                                "शिक्षा के माध्यम से सामाजिक परिवर्तन का संदेश",
                                "समीक्षात्मक प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 11,
                            title = "हँसते हुए मेरा अकेलापन (मलयाज)",
                            description = "डायरी विधा: लेखक का आत्ममंथन, बीमारी, सृजन और मानवीय संबंध",
                            topics = listOf(
                                "डायरी साहित्य का स्वरूप एवं मलयाज का दृष्टिकोण",
                                "अकेलापन, रचनात्मकता और यथार्थ का द्वंद्व",
                                "प्रकृति और मनुष्य के रिश्तों का चित्रण",
                                "डायरी अंशों की व्याख्या"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 12,
                            title = "तिरिछ (उदय प्रकाश)",
                            description = "जादुई यथार्थवाद: आधुनिक शहरी क्रूरता, भय और पिता की त्रासदी",
                            topics = listOf(
                                "तिरिछ का प्रतीकवाद एवं जादुई यथार्थ",
                                "शहरी असंवेदनशीलता और थाने/अस्पताल की क्रूरता",
                                "पिता का मानसिक और शारीरिक संघर्ष",
                                "कहानी की समीक्षा एवं प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 13,
                            title = "शिक्षा (जे. कृष्णमूर्ति)",
                            description = "दार्शनिक संभाषण: भयमुक्त शिक्षा, आंतरिक स्वतंत्रता और वास्तविक जीवन",
                            topics = listOf(
                                "सच्ची शिक्षा का उद्देश्य एवं भय से मुक्ति",
                                "महत्वाकांक्षा बनाम आंतरिक आनंद",
                                "क्रांति और नवीन समाज के निर्माण में शिक्षा की भूमिका",
                                "प्रमुख विचार एवं प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 14,
                            title = "विष्णु प्रभाकर एवं शिवपूजन सहाय रचनाएँ",
                            description = "हिंदी गद्य की विविध धाराएं एवं संस्मरण",
                            topics = listOf(
                                "शिवपूजन सहाय की गद्य शैली एवं देहाती दुनिया का संदर्भ",
                                "विष्णु प्रभाकर का मानवतावादी लेखन",
                                "अभ्यास प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 15,
                            title = "पंकज बिष्ट एवं पद्मा सचदेव रचनाएँ",
                            description = "समकालीन हिंदी गद्य एवं क्षेत्रीय चेतना",
                            topics = listOf(
                                "समकालीन सामाजिक समस्याएं एवं यथार्थपरक चित्रण",
                                "पद्मा सचदेव की आत्मीय गद्य शैली",
                                "गद्यांश व्याख्या एवं प्रश्नोत्तर"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 2,
                    unitTitle = "Unit II: Literature - Poetry (पद्य खण्ड)",
                    description = "Digant Part-2: 15 Poetry Chapters from Bhakti Kaal to Modern Hindi Poetry",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 16,
                            title = "कड़बक (मलिक मुहम्मद जायसी)",
                            description = "पद्मावत का अंश: बाह्य रूप की नश्वरता एवं आंतरिक गुण-प्रेम की अमरता",
                            topics = listOf(
                                "जायसी का कवि परिचय एवं सूफी प्रेममार्गी शाखा",
                                "कड़बक 1: रूपहीनता का गुणगान एवं एक आँख का चंद्रमा से सादृश्य",
                                "कड़बक 2: कीर्ति और यश की अमरता",
                                "काव्य सौंदर्य एवं पद्यांश व्याख्या"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 17,
                            title = "पद (सूरदास)",
                            description = "वात्सल्य रस के अमर पद: बाल कृष्ण को जगाने का सुंदर दृश्य",
                            topics = listOf(
                                "सूरदास का कृष्ण-वात्सल्य एवं ब्रजभाषा का माधुर्य",
                                "पद 1: 'जागिए ब्रजराज कुंवर' - प्रभात का प्राकृतिक वर्णन",
                                "पद 2: बाल कृष्ण का नंद की गोद में भोजन करना",
                                "सप्रसंग व्याख्या एवं प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 18,
                            title = "पद (तुलसीदास)",
                            description = "रामभक्ति एवं दास्य भाव: सीता जी के माध्यम से प्रभु राम को दैन्य निवेदन",
                            topics = listOf(
                                "तुलसीदास का भक्ति भाव एवं अवधी-ब्रज का समन्वय",
                                "पद 1: 'कबहूँक अंब अवसर पाइ' - सीता जी से प्रार्थना",
                                "पद 2: कलिकाल की विभीषिका एवं राम नाम का आश्रय",
                                "पद्यांश व्याख्या एवं काव्यगत सौंदर्य"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 19,
                            title = "छप्पय (नाभादास)",
                            description = "भक्तमाल: कबीरदास और सूरदास के काव्य-व्यक्तित्व का सटीक मूल्यांकन",
                            topics = listOf(
                                "नाभादास का भक्तमाल एवं छप्पय छंद",
                                "कबीर का निर्गुण विचार: पक्षपातरहित साखी और सबदी",
                                "सूरदास का सगुण चमत्कार एवं अनुप्रास सौंदर्य",
                                "प्रमुख छप्पय की व्याख्या"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 20,
                            title = "कवित्त (भूषण)",
                            description = "वीर रस: छत्रपति शिवाजी महाराज और छत्रसाल बुंदेला का ओजस्वी यशोगान",
                            topics = listOf(
                                "रीतिकाल में भूषण का वीर रस एवं राष्ट्रीय चेतना",
                                "कवित्त 1: छत्रपति शिवाजी का शौर्य (इंद्र, यम, राम, कृष्ण से उपमा)",
                                "कवित्त 2: छत्रसाल की तलवार का प्रलयंकारी रूप",
                                "अलंकार, बिंब और ओज गुण विश्लेषण"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 21,
                            title = "तुमुल कोलाहल कलह में (जयशंकर प्रसाद)",
                            description = "कामायनी (इड़ा सर्ग): श्रद्धा का मानवीय करुणा और शांति का संदेश",
                            topics = listOf(
                                "छायावाद एवं कामायनी महाकाव्य की पृष्ठभूमि",
                                "श्रद्धा का परिचय: व्याकुल मन के लिए हृदय की बात",
                                "रहस्यवाद, प्रतीक योजना और भाषा सौष्ठव",
                                "पद्यांश व्याख्या एवं प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 22,
                            title = "पुत्र वियोग (सुभद्रा कुमारी चौहान)",
                            description = "मुकुल से संकलित: एक माँ का अपने मृत पुत्र के प्रति असह्य विलाप",
                            topics = listOf(
                                "वात्सल्य का कारुणिक रूप एवं मातृ-हृदय की व्यथा",
                                "पुत्र के लालन-पालन और मन्नत मांगने का मार्मिक स्मरण",
                                "अलंकाररहित सहज और मर्मभेदी भाषा",
                                "सप्रसंग व्याख्या एवं प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 23,
                            title = "उषा (शमशेर बहादुर सिंह)",
                            description = "प्रयोगवादी कविता: भोर के प्राकृतिक सौंदर्य का जादुई बिंब-विधान",
                            topics = listOf(
                                "शमशेर का बिंब-विधान: 'राख से लीपा हुआ चौका', 'नील जल में झिलमिल देह'",
                                "सूर्योदय से पूर्व आकाश के बदलते रंगों का सजीव चित्रण",
                                "प्रयोगवाद की शिल्पगत विशेषताएं",
                                "काव्य सौंदर्य एवं प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 24,
                            title = "जन-जन का चेहरा एक (गजानन माधव मुक्तिबोध)",
                            description = "शोषित जनता का वैश्विक संघर्ष एवं क्रांतिकारी ऊर्जा का जागरण",
                            topics = listOf(
                                "मुक्तिबोध का फैंटेसी शिल्प और जनवादी स्वर",
                                "एशिया, यूरोप, अमेरिका के शोषितों की साझी पीड़ा और आक्रोश",
                                "क्रांति की ज्वाला एवं मानवीय संकल्प का जयघोष",
                                "कठिन काव्य पंक्तियों की व्याख्या"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 25,
                            title = "अधिनायक (रघुवीर सहाय)",
                            description = "व्यंग्य कविता: राष्ट्रीय गान में छिपे सत्ताधारी 'भारत भाग्य विधाता' पर चोट",
                            topics = listOf(
                                "समकालीन राजनीति पर तीखा कटाक्ष",
                                "हरचरना (आम जनता) का फटेहाल जीवन और राष्ट्रगान का औचित्य",
                                "व्यंग्य शैली, सपाटबयानी और भाषा का तंज",
                                "महत्वपूर्ण प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 26,
                            title = "प्यारे नन्हें बेटे को (विनोद कुमार शुक्ल)",
                            description = "लोहा की खोज: मेहनत, श्रम और जीवन संघर्ष का आत्मीय काव्यात्मक संवाद",
                            topics = listOf(
                                "पिता-पुत्री का सहज पारिवारिक संवाद",
                                "लोहा का प्रतीकार्थ: कुदाल, फावड़ा, खुरपी और हर मेहनतकश इंसान",
                                "सहज गद्य-कविता का माधुर्य एवं संदेश",
                                "पद्यांश व्याख्या"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 27,
                            title = "हार-जीत (अशोक वाजपेयी)",
                            description = "गद्य-कविता: युद्ध की निरर्थकता, उत्सव की अंधभक्ति और बूढ़े मशकवाले का सच",
                            topics = listOf(
                                "गद्य-कविता की विधा एवं अशोक वाजपेयी की शैली",
                                "जनता का बेखबर होना और सत्ता द्वारा विजयोत्सव का ढोंग",
                                "बूढ़े मशकवाले का वास्तविक यथार्थ",
                                "समीक्षा एवं प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 28,
                            title = "गाँव का घर (ज्ञानेंद्रपति)",
                            description = "संशयात्मा से: आधुनिकता के प्रभाव में लुप्त होती ग्रामीण संस्कृति और चौपाल",
                            topics = listOf(
                                "पारंपरिक गाँव के घर का सजीव चित्र (टिकुली, सहजन का पेड़, चौपाल)",
                                "आधुनिक टीवी, बिजली और खोते हुए लोकगीत (आल्हा, कजरी)",
                                "संस्कृति ह्रास पर कवि की गहरी चिंता",
                                "पद्यांश व्याख्या"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 29,
                            title = "ताज बीबी / घनानंद / रसखान पद",
                            description = "रीतिकाल एवं भक्तिकाल के मुक्तक प्रेम पद",
                            topics = listOf(
                                "घनानंद का स्वच्छंद प्रेम एवं विरह वेदना",
                                "ताज बीबी और भक्त कवियों का कृष्ण प्रेम",
                                "अभ्यास प्रश्नोत्तर"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 30,
                            title = "पूरक पाठन (प्रतिपूर्ति - 3 कहानियाँ)",
                            description = "विश्व साहित्य की तीन प्रतिनिधि कहानियाँ",
                            topics = listOf(
                                "रस्सी का टुकड़ा (गाय डी मोपांसा) का कथानक एवं चरित्र",
                                "क्लर्क की मौत (अंतान चेखव) का नौकरशाही व्यंग्य",
                                "पेशगी (हेनरी लोपेज) का राजनीतिक संघर्ष",
                                "पूरक पाठ के प्रमुख प्रश्नोत्तर"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 3,
                    unitTitle = "Unit III: Grammar & Composition (व्याकरण एवं रचना)",
                    description = "Comprehensive Hindi Vyakaran: Sandhi, Samas, Muhavare, Essay & Letter writing",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 31,
                            title = "वर्ण, संधि एवं शब्द-रचना",
                            description = "स्वर, व्यंजन, स्वर संधि, व्यंजन संधि, विसर्ग संधि",
                            topics = listOf(
                                "वर्ण विचार: स्वर एवं व्यंजन का वर्गीकरण व उच्चारण स्थान",
                                "स्वर संधि के 5 भेद: दीर्घ, गुण, वृद्धि, यण, अयादि (नियम एवं उदाहरण)",
                                "व्यंजन संधि के प्रमुख नियम एवं परिवर्तन",
                                "विसर्ग संधि के नियम एवं अभ्यास प्रश्न",
                                "उपसर्ग (संस्कृत, हिंदी, उर्दू/फारसी) एवं प्रत्यय (कृत् व तद्धित)"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 32,
                            title = "समास, पदबंध एवं वाक्य विचार",
                            description = "समास के 6 भेद, पदबंध, वाक्य भेद, रूपांतरण एवं शुद्धि",
                            topics = listOf(
                                "समास के 6 भेद: अव्ययीभाव, तत्पुरुष, कर्मधारय, द्विगु, द्वंद्व, बहुव्रीहि",
                                "समास विग्रह एवं सामासिक पद की पहचान",
                                "पदबंध: संज्ञा, सर्वनाम, विशेषण, क्रिया, क्रियाविशेषण पदबंध",
                                "वाक्य के प्रकार (रचना की दृष्टि से): सरल, संयुक्त, मिश्र वाक्य",
                                "वाक्य रूपांतरण (वाच्य: कर्तृवाच्य, कर्मवाच्य, भाववाच्य)",
                                "अशुद्ध वाक्य शोधन (लिंग, वचन, कारक, वर्तनी संबंधी अशुद्धियाँ)"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 33,
                            title = "शब्द संपदा एवं मुहावरे-लोकोक्तियाँ",
                            description = "पर्यायवाची, विलोम, अनेक शब्दों के लिए एक शब्द, मुहावरे, पारिभाषिक शब्दावली",
                            topics = listOf(
                                "पर्यायवाची शब्द (महत्वपूर्ण 100 शब्द)",
                                "विलोम (विपरीतार्थक) शब्द संग्रह",
                                "अनेक शब्दों के लिए एक शब्द (वाक्यांश के लिए एक शब्द)",
                                "मुहावरे एवं लोकोक्तियाँ: अर्थ एवं वाक्यों में प्रयोग",
                                "प्रशासनिक एवं तकनीकी पारिभाषिक शब्दावली (अंग्रेजी से हिंदी)"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 34,
                            title = "रचना कौशल (लेखन कला)",
                            description = "निबंध, पत्र-लेखन, संक्षेपण, वार्तालाप एवं टिप्पणी",
                            topics = listOf(
                                "निबंध लेखन (समसामयिक, सामाजिक, विज्ञान, पर्यावरण, राष्ट्रप्रेम)",
                                "पत्र लेखन: औपचारिक (प्रधानाचार्य, अधिकारी, संपादक) एवं अनौपचारिक",
                                "संक्षेपण (Précis Writing): नियम, शीर्षक चयन एवं एक-तिहाई सारांश",
                                "संवाद/वार्तालाप लेखन (Varta) एवं टिप्पणी लेखन (Tippani)",
                                "साहित्यिक संकल्पनाएं: बिंब (Imagery), प्रतीक (Symbols), रूपक (Metaphor)"
                            )
                        )
                    )
                )
            )
        )
    }

    fun getEnglishSyllabus(): MasterSubjectData {
        return MasterSubjectData(
            code = "ENGLISH",
            name = "English (Class XII)",
            iconName = "language",
            colorHex = "#B388FF",
            units = listOf(
                MasterUnitData(
                    unitNumber = 1,
                    unitTitle = "Unit I: Reading & Comprehension",
                    description = "Factual, discursive, and analytical unseen passages with vocabulary inference",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 1,
                            title = "Unseen Passages & Data Interpretation",
                            description = "Comprehension of discursive and factual texts, charts, and tables",
                            topics = listOf(
                                "Factual comprehension: Identifying explicit facts, statistics, and organizational sequence",
                                "Discursive comprehension: Analyzing reasoning, tone, argumentative logic, and viewpoints",
                                "Case-based factual passages with visual inputs (charts, graphs, survey tables)",
                                "Vocabulary skills in context: Synonyms, antonyms, idiom inference, and contextual meaning",
                                "Note-making format: Title, structured headings/subheadings, recognizable abbreviations, and summary"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 2,
                    unitTitle = "Unit II: Writing Skills & Advanced Composition",
                    description = "Notices, formal/informal letters, CV/resumes, reports, articles, debate & speech",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 2,
                            title = "Short & Medium Form Compositions",
                            description = "Notices, advertisements, invitations, and replies",
                            topics = listOf(
                                "Notice Writing (50 words): Issuing organization, clear heading, date, target audience, event details, contact",
                                "Advertisements: Classified ads (Situation Vacant/Wanted, To Let, Sale/Purchase, Lost & Found)",
                                "Formal and Informal Invitations (Card format vs letter format)",
                                "Acceptance and Refusal replies to invitations"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 3,
                            title = "Extended Formal Writing & Applications",
                            description = "Letters to Editor, job applications with bio-data/CV, official requests",
                            topics = listOf(
                                "Letter to the Editor: Highlighting social, environmental, civic, and educational issues",
                                "Application for a Job: Covering letter and comprehensive Bio-Data / Curriculum Vitae (CV)",
                                "Official complaints and inquiries to government bodies and public utility departments",
                                "Formal business emails and institutional memorandums"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 4,
                            title = "Discursive Writing & Article Composition",
                            description = "Articles, reports, speeches, debates, and précis writing",
                            topics = listOf(
                                "Article Writing (150-200 words): Catchy title, byline, introduction, cause-effect analysis, solutions",
                                "Report Writing for School Magazine or Newspaper: Date, venue, chronological event coverage",
                                "Speech Writing (Formal greeting, engaging opening quote, body points, inspiring conclusion)",
                                "Debate Writing: Presenting coherent arguments for or against a motion with rebuttal points",
                                "Précis Writing: Summarizing 300-word text into 100 words with appropriate title"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 3,
                    unitTitle = "Unit III: Grammar & Language Usage",
                    description = "Tenses, reported speech, active/passive voice, sentence synthesis, modals, idioms",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 5,
                            title = "Core Applied Grammar",
                            description = "Tense mastery, direct/indirect narration, voice transformations, clauses",
                            topics = listOf(
                                "Tense forms in narrative contexts (Reports, commentaries, literature essays)",
                                "Reported Speech (Direct to Indirect narration with statements, imperatives, interrogatives)",
                                "Active and Passive Voice in scientific, formal, and news discourses",
                                "Sentence Synthesis: Combining sentences into Simple, Compound, and Complex clauses",
                                "Modal Auxiliaries based on semantic considerations (Obligation, possibility, permission, necessity)",
                                "Phrasal Verbs, Prepositional idioms, and advanced vocabulary registers",
                                "Translation from regional language (Hindi) into English and vice versa"
                            )
                        )
                    )
                ),
                MasterUnitData(
                    unitNumber = 4,
                    unitTitle = "Unit IV: Literature (Textbook & Supplementary Reader)",
                    description = "Detailed prose, poems, drama, central themes, poetic devices & character analysis",
                    chapters = listOf(
                        MasterChapterData(
                            chapterNumber = 6,
                            title = "Prose Literature Section",
                            description = "Masterpieces of world and Indian English prose",
                            topics = listOf(
                                "The Last Lesson (Alphonse Daudet): Linguistic chauvinism, patriotism, Franz and M. Hamel",
                                "Lost Spring (Anees Jung): Child labor in Seemapuri ragpickers and Firozabad bangle makers",
                                "Deep Water (William Douglas): Overcoming phobia of water through sheer willpower and training",
                                "The Rattrap (Selma Lagerlöf): Metaphor of world as rattrap, human goodness, Edla Willmansson",
                                "Indigo (Louis Fischer): Gandhi's Champaran Satyagraha, civil disobedience, uplift of sharecroppers",
                                "Poets and Pancakes (Asokamitran): Gemini Studios culture, film making in early India, Stephen Spender",
                                "The Interview (Christopher Silvester): Ethics of interviews, Umberto Eco on semiotics and novels",
                                "Going Places (A.R. Barton): Adolescent hero-worship, fantasy vs reality, Sophie and Danny Casey"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 7,
                            title = "Poetry Literature Section",
                            description = "Themes, imagery, rhyme scheme, metaphors, and poetic devices",
                            topics = listOf(
                                "My Mother at Sixty-Six (Kamala Das): Aging, filial fear of separation, contrast with sprinting trees",
                                "Keeping Quiet (Pablo Neruda): Introspection, universal brotherhood, cessation of destructive activity",
                                "A Thing of Beauty (John Keats): Keatsian aesthetic theory, enduring joy, despondence vs beauty",
                                "A Roadside Stand (Robert Frost): Rural-urban economic divide, unfulfilled promises of politicians",
                                "Aunt Jennifer's Tigers (Adrienne Rich): Patriarchal oppression, art as immortal rebellion"
                            )
                        ),
                        MasterChapterData(
                            chapterNumber = 8,
                            title = "Supplementary Reader (Vistas Section)",
                            description = "Psychological and dramatic short stories",
                            topics = listOf(
                                "The Third Level (Jack Finney): Grand Central Station, time travel, escapism from modern anxieties",
                                "The Tiger King (Kalki): Satire on pride and mortality, Maharaja of Pratibandapuram",
                                "Journey to the End of the Earth (Tishani Doshi): Antarctica expedition, climate change evidence",
                                "The Enemy (Pearl S. Buck): Dr. Sadao Hoki, duty of a doctor vs patriotic nationalism during WWII",
                                "On the Face of It (Susan Hill): Derry and Mr. Lamb, physical disability, loneliness, positive optimism",
                                "Memories of Childhood (Zitkala-Sa & Bama): Racial discrimination and caste untouchability oppression"
                            )
                        )
                    )
                )
            )
        )
    }
}
