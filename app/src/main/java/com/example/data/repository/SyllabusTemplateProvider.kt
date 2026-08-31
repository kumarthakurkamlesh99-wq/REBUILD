package com.example.data.repository

data class SubjectTemplate(
    val name: String,
    val code: String,
    val iconName: String,
    val colorHex: String,
    val targetHours: Int,
    val chapters: List<String>
)

object SyllabusTemplateProvider {

    fun getTemplatesForStream(studentClass: String, stream: String): List<SubjectTemplate> {
        return when {
            stream.contains("PCM") -> listOf(
                SubjectTemplate(
                    name = "Physics",
                    code = "PHY",
                    iconName = "bolt",
                    colorHex = "#38E1FF",
                    targetHours = 120,
                    chapters = listOf(
                        "Electric Charges & Fields",
                        "Electrostatic Potential & Capacitance",
                        "Current Electricity",
                        "Moving Charges & Magnetism",
                        "Magnetism and Matter",
                        "Electromagnetic Induction",
                        "Alternating Current",
                        "Electromagnetic Waves",
                        "Ray Optics & Optical Instruments",
                        "Wave Optics",
                        "Dual Nature of Radiation & Matter",
                        "Atoms",
                        "Nuclei",
                        "Semiconductor Electronics"
                    )
                ),
                SubjectTemplate(
                    name = "Chemistry",
                    code = "CHEM",
                    iconName = "science",
                    colorHex = "#70B8FF",
                    targetHours = 110,
                    chapters = listOf(
                        "Solutions",
                        "Electrochemistry",
                        "Chemical Kinetics",
                        "d and f Block Elements",
                        "Coordination Compounds",
                        "Haloalkanes and Haloarenes",
                        "Alcohols, Phenols and Ethers",
                        "Aldehydes, Ketones & Carboxylic Acids",
                        "Amines",
                        "Biomolecules"
                    )
                ),
                SubjectTemplate(
                    name = "Mathematics",
                    code = "MATH",
                    iconName = "calculate",
                    colorHex = "#FFB300",
                    targetHours = 130,
                    chapters = listOf(
                        "Relations and Functions",
                        "Inverse Trigonometric Functions",
                        "Matrices",
                        "Determinants",
                        "Continuity and Differentiability",
                        "Application of Derivatives",
                        "Integrals",
                        "Application of Integrals",
                        "Differential Equations",
                        "Vector Algebra",
                        "Three Dimensional Geometry",
                        "Linear Programming",
                        "Probability"
                    )
                ),
                SubjectTemplate(
                    name = "English",
                    code = "ENG",
                    iconName = "menu_book",
                    colorHex = "#B388FF",
                    targetHours = 50,
                    chapters = listOf(
                        "The Last Lesson",
                        "Lost Spring",
                        "Deep Water",
                        "The Rattrap",
                        "Indigo",
                        "My Mother at Sixty-Six",
                        "Keeping Quiet",
                        "A Thing of Beauty",
                        "The Third Level",
                        "The Tiger King"
                    )
                )
            )

            stream.contains("PCB") && !stream.contains("PCMB") -> listOf(
                SubjectTemplate(
                    name = "Physics",
                    code = "PHY",
                    iconName = "bolt",
                    colorHex = "#38E1FF",
                    targetHours = 120,
                    chapters = listOf(
                        "Electric Charges & Fields",
                        "Electrostatic Potential & Capacitance",
                        "Current Electricity",
                        "Moving Charges & Magnetism",
                        "Magnetism and Matter",
                        "Electromagnetic Induction",
                        "Alternating Current",
                        "Electromagnetic Waves",
                        "Ray Optics & Optical Instruments",
                        "Wave Optics",
                        "Dual Nature of Radiation & Matter",
                        "Atoms",
                        "Nuclei",
                        "Semiconductor Electronics"
                    )
                ),
                SubjectTemplate(
                    name = "Chemistry",
                    code = "CHEM",
                    iconName = "science",
                    colorHex = "#70B8FF",
                    targetHours = 110,
                    chapters = listOf(
                        "Solutions",
                        "Electrochemistry",
                        "Chemical Kinetics",
                        "d and f Block Elements",
                        "Coordination Compounds",
                        "Haloalkanes and Haloarenes",
                        "Alcohols, Phenols and Ethers",
                        "Aldehydes, Ketones & Carboxylic Acids",
                        "Amines",
                        "Biomolecules"
                    )
                ),
                SubjectTemplate(
                    name = "Biology",
                    code = "BIO",
                    iconName = "eco",
                    colorHex = "#00E676",
                    targetHours = 140,
                    chapters = listOf(
                        "Sexual Reproduction in Flowering Plants",
                        "Human Reproduction",
                        "Reproductive Health",
                        "Principles of Inheritance and Variation",
                        "Molecular Basis of Inheritance",
                        "Evolution",
                        "Human Health and Disease",
                        "Microbes in Human Welfare",
                        "Biotechnology: Principles and Processes",
                        "Biotechnology and its Applications",
                        "Organisms and Populations",
                        "Ecosystem",
                        "Biodiversity and Conservation"
                    )
                ),
                SubjectTemplate(
                    name = "English",
                    code = "ENG",
                    iconName = "menu_book",
                    colorHex = "#B388FF",
                    targetHours = 50,
                    chapters = listOf(
                        "The Last Lesson",
                        "Lost Spring",
                        "Deep Water",
                        "The Rattrap",
                        "Indigo",
                        "My Mother at Sixty-Six",
                        "Keeping Quiet",
                        "A Thing of Beauty",
                        "The Third Level",
                        "The Tiger King"
                    )
                )
            )

            stream.contains("PCMB") -> listOf(
                SubjectTemplate(
                    name = "Physics",
                    code = "PHY",
                    iconName = "bolt",
                    colorHex = "#38E1FF",
                    targetHours = 110,
                    chapters = listOf(
                        "Electric Charges & Fields",
                        "Electrostatic Potential & Capacitance",
                        "Current Electricity",
                        "Moving Charges & Magnetism",
                        "Electromagnetic Induction & AC",
                        "Ray & Wave Optics",
                        "Modern Physics (Dual Nature, Atoms, Nuclei)",
                        "Semiconductors"
                    )
                ),
                SubjectTemplate(
                    name = "Chemistry",
                    code = "CHEM",
                    iconName = "science",
                    colorHex = "#70B8FF",
                    targetHours = 110,
                    chapters = listOf(
                        "Solutions & Electrochemistry",
                        "Chemical Kinetics",
                        "Inorganic (d & f Block, Coordination)",
                        "Organic Chemistry (Haloalkanes to Amines)",
                        "Biomolecules"
                    )
                ),
                SubjectTemplate(
                    name = "Mathematics",
                    code = "MATH",
                    iconName = "calculate",
                    colorHex = "#FFB300",
                    targetHours = 110,
                    chapters = listOf(
                        "Matrices & Determinants",
                        "Calculus (Continuity to Integrals)",
                        "Differential Equations",
                        "Vectors & 3D Geometry",
                        "Probability"
                    )
                ),
                SubjectTemplate(
                    name = "Biology",
                    code = "BIO",
                    iconName = "eco",
                    colorHex = "#00E676",
                    targetHours = 110,
                    chapters = listOf(
                        "Reproduction (Flowering Plants & Humans)",
                        "Genetics & Molecular Basis",
                        "Evolution",
                        "Human Health & Disease",
                        "Biotechnology & Ecology"
                    )
                ),
                SubjectTemplate(
                    name = "English",
                    code = "ENG",
                    iconName = "menu_book",
                    colorHex = "#B388FF",
                    targetHours = 40,
                    chapters = listOf(
                        "Prose Literature",
                        "Poetry Section",
                        "Vistas Supplementary",
                        "Writing Skills & Grammar"
                    )
                )
            )

            stream.contains("Commerce") -> listOf(
                SubjectTemplate(
                    name = "Accountancy",
                    code = "ACC",
                    iconName = "calculate",
                    colorHex = "#38E1FF",
                    targetHours = 120,
                    chapters = listOf(
                        "Accounting for Partnership Firms - Fundamentals",
                        "Goodwill: Nature and Valuation",
                        "Change in Profit-Sharing Ratio",
                        "Admission of a Partner",
                        "Retirement / Death of a Partner",
                        "Dissolution of a Partnership Firm",
                        "Accounting for Share Capital",
                        "Accounting for Debentures",
                        "Financial Statements of a Company",
                        "Cash Flow Statement"
                    )
                ),
                SubjectTemplate(
                    name = "Business Studies",
                    code = "BST",
                    iconName = "business_center",
                    colorHex = "#70B8FF",
                    targetHours = 100,
                    chapters = listOf(
                        "Nature and Significance of Management",
                        "Principles of Management",
                        "Business Environment",
                        "Planning",
                        "Organising",
                        "Staffing",
                        "Directing",
                        "Controlling",
                        "Financial Management",
                        "Financial Markets",
                        "Marketing Management",
                        "Consumer Protection"
                    )
                ),
                SubjectTemplate(
                    name = "Economics",
                    code = "ECO",
                    iconName = "trending_up",
                    colorHex = "#00E676",
                    targetHours = 110,
                    chapters = listOf(
                        "National Income and Related Aggregates",
                        "Money and Banking",
                        "Determination of Income and Employment",
                        "Government Budget and the Economy",
                        "Balance of Payments",
                        "Development Experience (1947-90) & Economic Reforms",
                        "Current Challenges Facing Indian Economy",
                        "Development Experience of India: A Comparison with Neighbours"
                    )
                ),
                SubjectTemplate(
                    name = "English",
                    code = "ENG",
                    iconName = "menu_book",
                    colorHex = "#B388FF",
                    targetHours = 50,
                    chapters = listOf(
                        "Flamingo Prose",
                        "Flamingo Poetry",
                        "Vistas Supplementary Reader",
                        "Reading Comprehension & Advanced Writing"
                    )
                )
            )

            stream.contains("Arts") || stream.contains("Humanities") -> listOf(
                SubjectTemplate(
                    name = "History",
                    code = "HIS",
                    iconName = "history_edu",
                    colorHex = "#FFB300",
                    targetHours = 110,
                    chapters = listOf(
                        "Bricks, Beads and Bones (Harappan)",
                        "Kings, Farmers and Towns (Early States)",
                        "Kinship, Caste and Class",
                        "Thinkers, Beliefs and Buildings",
                        "Through the Eyes of Travellers",
                        "Bhakti-Sufi Traditions",
                        "An Imperial Capital: Vijayanagara",
                        "Peasants, Zamindars and the State",
                        "Colonialism and the Countryside",
                        "Rebels and the Raj (1857)",
                        "Mahatma Gandhi and the Nationalist Movement",
                        "Framing the Constitution"
                    )
                ),
                SubjectTemplate(
                    name = "Political Science",
                    code = "POL",
                    iconName = "gavel",
                    colorHex = "#38E1FF",
                    targetHours = 100,
                    chapters = listOf(
                        "The End of Bipolarity",
                        "Contemporary Centres of Power",
                        "Contemporary South Asia",
                        "International Organisations",
                        "Security in the Contemporary World",
                        "Environment and Natural Resources",
                        "Globalisation",
                        "Challenges of Nation-Building",
                        "Era of One-Party Dominance",
                        "Politics of Planned Development",
                        "India's External Relations",
                        "Democratic Resurgence"
                    )
                ),
                SubjectTemplate(
                    name = "Geography",
                    code = "GEO",
                    iconName = "public",
                    colorHex = "#00E676",
                    targetHours = 100,
                    chapters = listOf(
                        "Human Geography: Nature and Scope",
                        "The World Population: Distribution, Density and Growth",
                        "Human Development",
                        "Primary Activities",
                        "Secondary Activities",
                        "Tertiary and Quaternary Activities",
                        "Transport and Communication",
                        "International Trade"
                    )
                ),
                SubjectTemplate(
                    name = "English",
                    code = "ENG",
                    iconName = "menu_book",
                    colorHex = "#B388FF",
                    targetHours = 50,
                    chapters = listOf(
                        "Literature Section (Flamingo)",
                        "Poetry Analysis",
                        "Supplementary Reader (Vistas)",
                        "Creative Writing Skills"
                    )
                )
            )

            else -> listOf(
                SubjectTemplate(
                    name = "Core Subject 1",
                    code = "SUB1",
                    iconName = "menu_book",
                    colorHex = "#38E1FF",
                    targetHours = 100,
                    chapters = listOf(
                        "Chapter 1: Foundations",
                        "Chapter 2: Core Concepts",
                        "Chapter 3: Advanced Theory",
                        "Chapter 4: Practical Applications",
                        "Chapter 5: Problem Solving",
                        "Chapter 6: Revision & PYQs"
                    )
                ),
                SubjectTemplate(
                    name = "Core Subject 2",
                    code = "SUB2",
                    iconName = "science",
                    colorHex = "#70B8FF",
                    targetHours = 100,
                    chapters = listOf(
                        "Chapter 1: Principles",
                        "Chapter 2: Analytical Methods",
                        "Chapter 3: Deep Mastery",
                        "Chapter 4: Mock Tests & Review"
                    )
                ),
                SubjectTemplate(
                    name = "General Knowledge & Strategy",
                    code = "GEN",
                    iconName = "psychology",
                    colorHex = "#00E676",
                    targetHours = 60,
                    chapters = listOf(
                        "Module 1: High Yield Concepts",
                        "Module 2: Formulas & Mind Maps",
                        "Module 3: Previous Year Papers",
                        "Module 4: Exam Simulation"
                    )
                )
            )
        }
    }
}
