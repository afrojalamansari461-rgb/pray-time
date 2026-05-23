package com.example.data

data class HadithItem(
    val id: String,
    val text: String,
    val narration: String,
    val topic: String
)

data class DuaItem(
    val id: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val occasion: String
)

object HadithDuaData {

    val HADITH_TOPICS = listOf("All", "Intention & Sincerity", "Knowledge & Wisom", "Character & Manners", "Kindness & Charity", "Prayer & Remembrance")

    val HADITHS = listOf(
        HadithItem(
            id = "hadith_1",
            text = "Actions are judged by intentions, and every person will get what they intended.",
            narration = "Sahih al-Bukhari & Sahih Muslim",
            topic = "Intention & Sincerity"
        ),
        HadithItem(
            id = "hadith_2",
            text = "The best among you are those who have the best manners and character.",
            narration = "Sahih al-Bukhari",
            topic = "Character & Manners"
        ),
        HadithItem(
            id = "hadith_3",
            text = "Whoever takes a path upon which to obtain knowledge, Allah makes the path to Paradise easy for him.",
            narration = "Sahih Muslim",
            topic = "Knowledge & Wisom"
        ),
        HadithItem(
            id = "hadith_4",
            text = "Cleanliness is half of faith.",
            narration = "Sahih Muslim",
            topic = "Character & Manners"
        ),
        HadithItem(
            id = "hadith_5",
            text = "Every act of goodness is a charity. Smiling in the face of your brother is charity.",
            narration = "Sahih Muslim & Jami' al-Tirmidhi",
            topic = "Kindness & Charity"
        ),
        HadithItem(
            id = "hadith_6",
            text = "Allah does not look at your appearances or your wealth, but He looks at your hearts and your actions.",
            narration = "Sahih Muslim",
            topic = "Intention & Sincerity"
        ),
        HadithItem(
            id = "hadith_7",
            text = "The strong man is not the one who can wrestle, but the one who can control himself when angry.",
            narration = "Sahih al-Bukhari & Sahih Muslim",
            topic = "Character & Manners"
        ),
        HadithItem(
            id = "hadith_8",
            text = "Establish prayer, for prayer restrains from shameful and unjust deeds.",
            narration = "Al-Ankabut 45 (Quoted in Ahadith)",
            topic = "Prayer & Remembrance"
        ),
        HadithItem(
            id = "hadith_9",
            text = "The closest a servant comes to his Lord is when he is in prostration (Sajdah), so increase supplication in it.",
            narration = "Sahih Muslim",
            topic = "Prayer & Remembrance"
        ),
        HadithItem(
            id = "hadith_10",
            text = "Give charity without delay, for it stands in the way of calamity.",
            narration = "Al-Tirmidhi",
            topic = "Kindness & Charity"
        )
    )

    val DUA_OCCASIONS = listOf("All", "Morning & Evening", "Daily Life", "Ramadan & Fasting", "Seeking Help")

    val DUAS = listOf(
        DuaItem(
            id = "dua_1",
            title = "Dua for Morning",
            arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ",
            transliteration = "Asbahna wa-asbahal-mulku lillah, wal-hamdulillahi",
            translation = "We have entered a new day and with it all sovereignty remains Allah's, and all praise is due to Allah.",
            occasion = "Morning & Evening"
        ),
        DuaItem(
            id = "dua_2",
            title = "Dua for Evening",
            arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ",
            transliteration = "Amsayna wa-amsal-mulku lillah, wal-hamdulillahi",
            translation = "Since evening has come to us, sovereignty remains Allah's, and all praise is due to Allah.",
            occasion = "Morning & Evening"
        ),
        DuaItem(
            id = "dua_3",
            title = "Dua Before Eating",
            arabic = "بِسْمِ اللَّهِ وَعَلَى بَرَكَةِ اللَّهِ",
            transliteration = "Bismillahi wa 'ala barakatillah",
            translation = "In the name of Allah and upon the blessings of Allah.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_4",
            title = "Dua After Eating",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
            transliteration = "Alhamdu lillahil-ladhi at'amana wa saqana wa ja'alana Muslimeen",
            translation = "Praise be to Allah who has fed us and given us drink and made us Muslims.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_5",
            title = "Dua for breaking fast (Iftar)",
            arabic = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ",
            transliteration = "Dhahaba-zama'u wabtallatil-'uruqu wa thabatal-ajru in sha' Allah",
            translation = "The thirst is gone, the veins are moistened, and the reward is confirmed, if Allah wills.",
            occasion = "Ramadan & Fasting"
        ),
        DuaItem(
            id = "dua_6",
            title = "Dua for Intent of Fasting",
            arabic = "وَبِصَوْمِ غَدٍ نَّوَيْتُ مِنْ شَهْرِ رَمَضَانَ",
            transliteration = "Wa bi-sawmi ghadinn nawaytu min shahri ramadan",
            translation = "I intend to keep the fast tomorrow for the month of Ramadan.",
            occasion = "Ramadan & Fasting"
        ),
        DuaItem(
            id = "dua_7",
            title = "Dua for Grief and Patience",
            arabic = "رَبَّنَا أَفْرِغْ عَلَيْنَا صَبْرًا وَتَوَفَّنَا مُسْلِمِينَ",
            transliteration = "Rabbana afrigh 'alayna sabran wa tawaffana Muslimeen",
            translation = "Our Lord, pour upon us patience and let us die in submission to You.",
            occasion = "Seeking Help"
        ),
        DuaItem(
            id = "dua_8",
            title = "Dua for Seeking Forgiveness",
            arabic = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ الَّذِي لاَ إِلَهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullahal-'Azeemal-ladhi la ilaha illa Huwal-Hayyul-Qayyum wa atubu ilayh",
            translation = "I seek forgiveness from Allah the Almighty, besides Whom there is no God, the Living, the Sustainer, and I turn to Him.",
            occasion = "Seeking Help"
        )
    )
}
