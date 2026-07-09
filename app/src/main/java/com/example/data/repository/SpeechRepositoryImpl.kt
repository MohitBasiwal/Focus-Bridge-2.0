package com.example.data.repository

import com.example.data.database.dao.SpeechDao
import com.example.data.database.entity.EducationalParagraphEntity
import com.example.data.database.entity.SpeechConfigEntity
import com.example.domain.repository.SpeechRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class SpeechRepositoryImpl @Inject constructor(
    private val speechDao: SpeechDao
) : SpeechRepository {

    override fun getSpeechConfigFlow(): Flow<SpeechConfigEntity?> {
        return speechDao.getSpeechConfigFlow()
    }

    override suspend fun getSpeechConfigSnapshot(): SpeechConfigEntity? {
        var config = speechDao.getSpeechConfig()
        if (config == null) {
            config = SpeechConfigEntity(
                selectedCategories = "Science,Mathematics,History,Geography,Technology,Biology,Physics,Chemistry,General Knowledge,English Vocabulary",
                currentParagraphId = null,
                bypassUntilMs = 0L
            )
            speechDao.saveSpeechConfig(config)
        }
        return config
    }

    override suspend fun saveSelectedCategories(categories: List<String>) {
        val current = getSpeechConfigSnapshot() ?: return
        val commaSeparated = categories.joinToString(",")
        speechDao.saveSpeechConfig(
            current.copy(
                selectedCategories = commaSeparated,
                currentParagraphId = null // Reset active challenge paragraph so a matching category is chosen
            )
        )
    }

    override suspend fun selectAndLockRandomParagraph(): EducationalParagraphEntity? {
        seedParagraphsIfNeeded()
        val current = getSpeechConfigSnapshot() ?: return null

        // If we already have a locked paragraph, return it
        if (current.currentParagraphId != null) {
            val locked = speechDao.getParagraphById(current.currentParagraphId)
            if (locked != null) return locked
        }

        // Parse chosen categories
        val chosenCategories = if (current.selectedCategories.isBlank()) {
            emptyList()
        } else {
            current.selectedCategories.split(",").map { it.trim() }
        }

        val candidates = if (chosenCategories.isEmpty()) {
            speechDao.getAllParagraphs()
        } else {
            speechDao.getParagraphsByCategories(chosenCategories)
        }

        if (candidates.isEmpty()) return null

        val randomParagraph = candidates[Random.nextInt(candidates.size)]
        
        // Lock this paragraph
        speechDao.saveSpeechConfig(
            current.copy(currentParagraphId = randomParagraph.id)
        )

        return randomParagraph
    }

    override suspend fun getCurrentParagraph(): EducationalParagraphEntity? {
        val current = getSpeechConfigSnapshot() ?: return null
        return if (current.currentParagraphId != null) {
            speechDao.getParagraphById(current.currentParagraphId)
        } else {
            selectAndLockRandomParagraph()
        }
    }

    override suspend fun setTemporaryUnlockBypass(durationMinutes: Int) {
        val current = getSpeechConfigSnapshot() ?: return
        val bypassEnd = System.currentTimeMillis() + (durationMinutes * 60 * 1000)
        speechDao.saveSpeechConfig(
            current.copy(
                bypassUntilMs = bypassEnd,
                currentParagraphId = null // Reset current paragraph challenge as it is now successfully completed
            )
        )
    }

    override suspend fun clearBypass() {
        val current = getSpeechConfigSnapshot() ?: return
        speechDao.saveSpeechConfig(
            current.copy(bypassUntilMs = 0L)
        )
    }

    override suspend fun resetCurrentParagraph() {
        val current = getSpeechConfigSnapshot() ?: return
        speechDao.saveSpeechConfig(
            current.copy(currentParagraphId = null)
        )
    }

    override suspend fun seedParagraphsIfNeeded() {
        val count = speechDao.getParagraphCount()
        if (count > 0) return

        val initialParagraphs = listOf(
            EducationalParagraphEntity(
                category = "Science",
                text = "The scientific method is a systematic process of empirical inquiry that forms the bedrock of modern scientific discovery. By formulating testable hypotheses, executing rigorous experiments, and gathering precise observations, scientists can uncover the fundamental laws governing physical reality. Throughout history, groundbreaking insights have revolutionized our understanding of existence. Albert Einstein's theory of relativity shattered classical notions of absolute space and time, demonstrating that gravity is actually the curvature of the spacetime fabric caused by mass. Simultaneously, quantum mechanics emerged to explain the bizarre and probabilistic behavior of matter and energy at subatomic scales, where particles can exist in multiple states at once until observed. From investigating the vast, expanding cosmic web of galaxies to mapping the infinitesimally small building blocks of matter, science continues to push the boundaries of human knowledge. It encourages a mindset of healthy skepticism, where theories are constantly tested, refined, or replaced in light of new evidence. Embracing scientific literacy is critical for addressing global challenges, including environmental conservation, disease eradication, and sustainable development. As we venture further into the twenty-first century, our reliance on scientific progress grows exponentially. We must cultivate a deep curiosity about the natural world, supporting research that seeks to understand both the depths of the oceans and the edge of the observable universe. By doing so, we ensure that humanity's search for truth remains a guided, progressive, and endlessly inspiring journey through the cosmos."
            ),
            EducationalParagraphEntity(
                category = "Mathematics",
                text = "Mathematics is often described as the universal language of the cosmos, providing the foundational framework for analyzing patterns, structures, and relationships in our universe. Far from being a mere collection of formulas, mathematics is a creative endeavor that explores the logical consequences of simple starting assumptions. Consider prime numbers, which are numbers greater than one divisible only by themselves and one. These fundamental integers act as the basic building blocks of all whole numbers, playing a critical role in modern digital cryptography and securing our global communications. Another fascinating concept is the Fibonacci sequence, where each term is the sum of the preceding two. This simple additive pattern manifests beautifully in nature, describing the spiral arrangement of sunflower seeds, the structure of pinecones, and the proportions of spiral galaxies. Furthermore, calculus, developed independently by Isaac Newton and Gottfried Wilhelm Leibniz, allows us to model continuous change, enabling modern engineering, space exploration, and economic prediction. Through abstraction, mathematics reveals deep, unexpected connections between seemingly unrelated phenomena. It teaches us to think with absolute precision, constructing rigorous proofs that establish truths with certainty. Whether we are calculating the trajectories of planets, designing complex architectural wonders, or developing deep learning algorithms, mathematics serves as an indispensable tool. It empowers us to perceive a hidden harmony in the chaotic noise of the world, reminding us that reality is structured in an elegant, mathematically consistent manner that continues to inspire awe."
            ),
            EducationalParagraphEntity(
                category = "History",
                text = "History is the comprehensive study of the human past, a rich tapestry of events, ideas, conflicts, and achievements that have shaped the contemporary world. By analyzing the decisions and cultures of ancient civilizations, we gain invaluable insights into human behavior and the complex evolution of modern societies. The rise and fall of empires, such as the Roman, Han, and Mayan dynasties, teach us about the dynamics of political power, trade routes, and social organization. Similarly, major historical milestones like the European Renaissance and the subsequent scientific revolution demonstrated how intellectual shifts can trigger profound changes in technology, art, and daily life. The Industrial Revolution of the eighteenth century fundamentally restructured global economies, transforming agricultural societies into urban industrial powerhouses, while introducing challenges regarding labor and resources. Studying history is not merely about memorizing names and dates; it is a vital exercise in empathy and critical thinking. It requires us to evaluate diverse primary sources, recognize systemic patterns, and understand the historical roots of current geopolitical events. By understanding our shared triumphs and devastating conflicts, we are better equipped to avoid past mistakes and construct a more just, peaceful, and cooperative future. Every generation reconstructs its understanding of the past, finding new meanings and lessons that speak directly to contemporary struggles. In this way, history remains a living, breathing dialogue between the past and the present, guiding our footsteps as we navigate the challenges of the future."
            ),
            EducationalParagraphEntity(
                category = "Geography",
                text = "Geography is the dynamic study of Earth's landscapes, peoples, environments, and the complex relationships that exist between them. It serves as a vital bridge between the physical and social sciences, analyzing how spatial distributions impact human development and ecological stability. At the core of physical geography is the theory of plate tectonics, which explains how massive lithospheric plates shift on the fluid mantle below. This movement forms towering mountain ranges, triggers volcanic activity, and shapes the continents over hundreds of millions of years. Simultaneously, atmospheric circulation and ocean currents distribute heat across the planet, creating diverse climate zones that support unique biomes, from lush tropical rainforests to hyper-arid deserts. Human geography examines how populations distribute themselves, build cities, cultivate land, and adapt to their local geographic constraints. Mountain barriers, deep rivers, and vast oceans have historically determined migration paths, trade routes, and cultural boundaries. Today, geographic information systems, or GIS, allow us to map and analyze spatial data with unprecedented accuracy, helping us tackle urban planning, deforestation, and natural resource management. As human activity increasingly alters global ecosystems, understanding geography becomes essential for fostering sustainable coexistence with our planet. By analyzing the delicate feedback loops between human systems and physical environments, geographers work to mitigate climate change, protect biodiversity, and manage scarce water resources. Exploring geography teaches us to appreciate the planet's vast diversity, instilling a profound sense of stewardship for the fragile, interconnected world we call home."
            ),
            EducationalParagraphEntity(
                category = "Technology",
                text = "Technology represents the practical application of scientific knowledge to solve real-world problems, extending human capabilities and transforming every aspect of daily life. From the invention of the wheel to the development of silicon microchips, technological innovation has been the primary driver of human progress. In the modern era, the rapid rise of digital technology has created an interconnected global society, redefining how we communicate, work, and learn. The internet, arguably the most significant technological milestone in human history, democratized access to information, allowing people across the globe to share ideas instantaneously. Today, we are witnessing the emergence of artificial intelligence, machine learning, and quantum computing, which promise to revolutionize fields ranging from healthcare diagnostics to climate modeling. Silicon microprocessors, which execute billions of instructions per second, have become the silent engines powering everything from smartphones to smart grids. However, rapid technological advancement introduces complex ethical questions, including issues of digital privacy, automation-induced job displacement, and the digital divide. As we develop increasingly sophisticated tools, it is crucial to ensure that technology is designed and deployed responsibly, serving to uplift communities and preserve democratic values. We must foster digital literacy and encourage inclusive education, preparing future generations to navigate a landscape shaped by biotechnology, clean energy grids, and space exploration. Ultimately, technology is a reflection of human ambition and ingenuity; it is a powerful tool whose impact is determined by the wisdom, ethics, and collective values of those who create and use it."
            ),
            EducationalParagraphEntity(
                category = "Biology",
                text = "Biology is the scientific study of life, exploring the staggering diversity, structure, function, and evolution of living organisms. From microscopic single-celled bacteria to colossal blue whales, biology seeks to understand the fundamental processes that sustain life on Earth. At the molecular level, DNA serves as the universal genetic code, storing the precise instructions required for building and maintaining every organism. Cellular biology investigates how these instructions are translated into functional proteins, driving cellular mitosis and energy production. Organisms do not exist in isolation; they are deeply integrated into complex ecosystems, where they engage in intricate relationships. Photosynthesis, executed by plants and algae, is a cornerstone of global life, converting solar energy into chemical energy while producing the oxygen that supports respiration in animals. Evolution by natural selection, proposed by Charles Darwin, is the unifying theory of biology, explaining how species adapt to shifting environmental pressures over generations, resulting in the rich biodiversity we observe today. Modern biological research is rapidly expanding, with breakthroughs in gene editing, synthetic biology, and ecological conservation offering solutions to urgent global crises. CRISPR technology allows scientists to make precise edits to genomes, offering the potential to cure genetic diseases and develop resilient agricultural crops. At the same time, conservation biology works tirelessly to protect endangered species and restore damaged habitats, recognizing that human survival is intrinsically tied to the health of the biosphere. By studying biology, we develop a deep respect for the complexity of living systems, reinforcing our responsibility to preserve the web of life."
            ),
            EducationalParagraphEntity(
                category = "Physics",
                text = "Physics is the fundamental science that seeks to understand the behavior of matter, energy, space, and time at the most basic levels. It investigates the underlying principles that govern everything from the motion of subatomic quarks to the expansion of the entire universe. At the heart of classical physics are Isaac Newton's laws of motion and gravity, which explain the trajectories of thrown objects and the orbits of planets. However, the early twentieth century brought radical shifts that transformed our view of the cosmos. Albert Einstein introduced his theories of relativity, showing that space and time are dynamically linked in a four-dimensional fabric called spacetime, which warps in the presence of mass and energy. This warping is what we perceive as gravity. In the microscopic realm, quantum mechanics revealed a probabilistic world where energy is quantized, and particles exhibit wave-like behaviors, defying classical intuition. Thermodynamics describes how energy moves and transforms, introducing the concept of entropy, which dictates the irreversible direction of time. Today, physicists pursue a grand unified theory that can bridge the gap between quantum mechanics and general relativity, potentially unlocking the mysteries of dark matter, dark energy, and the origins of the Big Bang. From engineering semiconductors and medical imaging devices to developing fusion energy, the practical applications of physics drive our technological civilization. By questioning the nature of reality and conducting rigorous experiments, physics continues to expand human horizons, demonstrating that the cosmos is governed by mathematical laws we can comprehend."
            ),
            EducationalParagraphEntity(
                category = "Chemistry",
                text = "Chemistry is the central science that studies the composition, structure, properties, and transformations of matter. It serves as a crucial link connecting physics, which explains fundamental forces, with biology, which studies complex living systems. At the foundation of chemistry is the atomic theory, which states that all matter is composed of tiny, indivisible units called atoms. The periodic table organizes these elements based on their atomic number and chemical behavior, revealing patterns of reactivity that allow chemists to predict how substances interact. Chemical reactions occur when atoms share or transfer valence electrons to form stable covalent or ionic bonds, transforming starting reactants into entirely new molecular products. Thermodynamics governs the energy changes during these reactions, determining whether a process releases heat or requires an input of external energy. Organic chemistry focuses on carbon-based compounds, which form the structural basis of all known life, while inorganic chemistry explores metals, minerals, and materials. Analytical chemistry develops precise methods to identify and measure substances, ensuring the safety of our food, water, and pharmaceuticals. Today, green chemistry works to design chemical processes that minimize waste and reduce environmental hazards, promoting sustainable development. From synthesizing life-saving medicines and designing high-capacity batteries to understanding the chemical signatures of distant planets, chemistry is essential for solving modern global challenges. It empowers us to manipulate matter at the molecular level, demonstrating that the countless materials in our world are built from simple, elegant atomic combinations."
            ),
            EducationalParagraphEntity(
                category = "General Knowledge",
                text = "General knowledge encompasses a broad, diverse understanding of cultural, scientific, historical, and contemporary facts about our world, fostering well-rounded intellect and critical awareness. Cultivating general knowledge allows individuals to engage in meaningful conversations, analyze global events, and make informed daily decisions. For instance, knowing that our solar system consists of eight planets orbiting a medium-sized star called the Sun helps us appreciate Earth's unique position in supporting life. Similarly, understanding that oceans cover over seventy percent of the planet's surface highlights the critical role marine ecosystems play in regulating global climate patterns and producing oxygen. Historically, general knowledge includes awareness of monumental achievements like the drafting of the Universal Declaration of Human Rights, which established fundamental freedoms for all global citizens. In the modern era, general knowledge extends to understanding global economic systems, renewable energy technologies, and the importance of digital literacy in an interconnected world. Fostering this awareness is a lifelong pursuit that requires curiosity, diverse reading habits, and active engagement with the world. It prevents narrow-mindedness, encouraging empathy by exposing us to different cultures, ideas, and scientific perspectives. Ultimately, general knowledge is the foundation upon which specialized expertise is built, providing the context necessary to solve complex, interdisciplinary problems. By continually expanding our understanding of both trivial and profound facts, we become active, informed citizens who are better prepared to navigate and contribute to our rapidly changing global civilization."
            ),
            EducationalParagraphEntity(
                category = "English Vocabulary",
                text = "English vocabulary is a rich, dynamic system of words that has evolved over centuries, drawing from a diverse array of linguistic origins. Understanding the etymology, or history, of English words reveals how Latin, Greek, French, and Germanic roots have combined to create a highly expressive language. For example, Latin prefixes like 'bene' meaning good, and Greek suffixes like 'ology' meaning study of, form the basis of countless academic and scientific terms. Building a sophisticated vocabulary is not about memorizing obscure dictionary definitions; rather, it is about enhancing our ability to communicate thoughts with nuance, precision, and emotional resonance. A rich lexicon allows writers and speakers to select the exact word needed to convey a specific meaning, avoiding ambiguity and capturing subtle distinctions in thought. For instance, distinguishing between 'transient' and 'ephemeral', or 'eloquent' and 'loquacious', adds sophistication to both analysis and expression. Furthermore, a strong vocabulary is directly correlated with reading comprehension, academic success, and professional advancement. It empowers us to interpret complex texts, engage in persuasive debate, and express ourselves clearly in writing. The English language is constantly expanding, adopting new words from technology, slang, and global cultures, reflecting the evolving needs of its speakers. By actively studying word origins, synonyms, and literary techniques, we deepen our appreciation for the beauty and power of language. Ultimately, expanding our vocabulary provides us with the keys to unlock new ideas, enabling us to participate fully in the global conversation and share our unique perspectives with the world."
            )
        )
        speechDao.insertParagraphs(initialParagraphs)
    }
}
