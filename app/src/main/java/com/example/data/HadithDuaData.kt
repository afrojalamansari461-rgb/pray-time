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
        ),
        DuaItem(
            id = "dua_9",
            title = "Dua upon Waking Up",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdu lillahil-ladhi ahyana ba'da ma amatana wa ilayhin-nushur",
            translation = "All praise is for Allah who gave us life after having taken it from us, and unto Him is the resurrection.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_10",
            title = "Dua before Entering Washroom",
            arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ",
            transliteration = "Allahumma inni a'udhu bika minal-khubutha wal-khaba'ith",
            translation = "O Allah, I seek refuge with You from all offensive and noxious things (evil spirits).",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_11",
            title = "Dua after Leaving Washroom",
            arabic = "غُفْرَانَكَ",
            transliteration = "Ghufranak",
            translation = "I seek Your forgiveness.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_12",
            title = "Dua when Leaving House",
            arabic = "بِسْمِ اللَّهِ ، تَوَكَّلْتُ عَلَى اللَّهِ ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Bismillahi tawakkaltu 'alallah, wa la hawla wa la quwwata illa billah",
            translation = "In the name of Allah, I place my trust in Allah, and there is no might nor power except with Allah.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_13",
            title = "Dua when Entering House",
            arabic = "بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَى اللَّهِ رَبِّنَا تَوَكَّلْنَا",
            transliteration = "Bismillahi walajna, wa bismillahi kharajna, wa 'ala Allahi rabbina tawakkalna",
            translation = "In the name of Allah we enter, in the name of Allah we leave, and upon Allah our Lord we rely.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_14",
            title = "Dua when Entering Mosque",
            arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliteration = "Allahummaf-tah li abwaba rahmatik",
            translation = "O Allah, open for me the gates of Your mercy.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_15",
            title = "Dua when Leaving Mosque",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            transliteration = "Allahumma inni as'aluka min fadlik",
            translation = "O Allah, I ask You from Your favor.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_16",
            title = "Dua before Sleeping",
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allahumma amutu wa ahya",
            translation = "In Your name, O Allah, I die and I live.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_17",
            title = "Dua when Wearing Clothes",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي كَسَانِي هَذَا الثَّوْبَ وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            transliteration = "Alhamdu lillahil-ladhi kasani hadha-thawba wa razaqanihi min ghayri hawlim-minni wa la quwwatin",
            translation = "Praise be to Allah who has clothed me with this garment and provided it for me without any might or power on my part.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_18",
            title = "Dua after performing Wudu",
            arabic = "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            transliteration = "Ashhadu an la ilaha illallahu wahdahu la sharika lahu wa ashhadu anna Muhammadan 'abduhu wa Rasuluhu",
            translation = "I bear witness that there is no deity worthy of worship except Allah, alone without partner, and that Muhammad is His servant and Messenger.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_19",
            title = "Dua after hearing the Adhan",
            arabic = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّداً الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَاماً مَحْمُوداً الَّذِي وَعَدْتَهُ",
            transliteration = "Allahumma Rabba hadhihid-da'watit-tammah, was-salatil-qai'mah, ati Muhammadanil-wasilata wal-fadilah, wab'athhu maqamam-mahmudanil-ladhi wa'adtah",
            translation = "O Allah, Lord of this perfect call and established prayer, grant Muhammad the intercession and favor, and raise him to the praised station which You have promised.",
            occasion = "Morning & Evening"
        ),
        DuaItem(
            id = "dua_20",
            title = "Dua for Traveling",
            arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
            transliteration = "Subhanal-ladhi sakhkhara lana hadha wa ma kunna lahu muqrineen, wa inna ila Rabbina lamunqaliboon",
            translation = "Glory to Him Who has subjected this to us, whereas we were not able to have it, and surely to our Lord we are returning.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_21",
            title = "Dua when Looking in the Mirror",
            arabic = "اللَّهُمَّ أَنْتَ حَسَّنْتَ خَلْقِي فَحَسِّنْ خُلُقِي",
            transliteration = "Allahumma Anta hassanta khalqi fa hassin khuluqi",
            translation = "O Allah, just as You have made my physical appearance beautiful, make my character beautiful as well.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_22",
            title = "Dua for Parents",
            arabic = "رَّبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
            transliteration = "Rabbir-hamhuma kama rabbayani sagheera",
            translation = "My Lord, have mercy upon them [my parents] as they brought me up when I was small.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_23",
            title = "Dua upon Sneezing",
            arabic = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdulillah",
            translation = "All praise is for Allah.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_24",
            title = "Dua upon hearing someone sneeze",
            arabic = "يَرْحَمُكَ اللَّهُ",
            transliteration = "Yarhamukallah",
            translation = "May Allah have mercy on you.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_25",
            title = "Dua for Seeking Well-being & Protection ('Afiyah)",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ",
            transliteration = "Allahumma inni as'aluka-l-'afwa wa-l-'afiyata fid-dunya wal-akhirah",
            translation = "O Allah, I ask You for forgiveness and safety/well-being in this world and the Hereafter.",
            occasion = "Morning & Evening"
        ),
        DuaItem(
            id = "dua_26",
            title = "Dua when experiencing Distress or Anxiety",
            arabic = "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
            transliteration = "La ilaha illa Anta subhanaka inni kuntu minaz-zalimeen",
            translation = "There is no deity worthy of worship except You; exalted are You. Indeed, I have been of the wrongdoers.",
            occasion = "Daily Life"
        ),
        DuaItem(
            id = "dua_27",
            title = "Dua for Protection from Evil and Harm",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-ladhi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Aleem",
            translation = "In the name of Allah, with Whose name nothing can cause harm on earth or in the heaven, and He is the All-Hearing, the All-Knowing.",
            occasion = "Morning & Evening"
        )
    )
}
