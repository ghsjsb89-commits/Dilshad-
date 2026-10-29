package com.example.data

import com.example.model.DuaCategory
import com.example.model.IslamicDua
import com.example.model.PrayerTime
import com.example.model.QuranAyah
import java.util.Calendar
import java.util.Locale
import kotlin.math.*

object IslamicRepository {

    val quranVerses = listOf(
        QuranAyah(
            surahNumber = 2,
            surahNameArabic = "البقرة",
            surahNameEnglish = "Al-Baqarah",
            ayahNumber = 255,
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ",
            urduTranslation = "اللہ تعالیٰ کے سوا کوئی معبود نہیں، وہ زندہ ہے اور سب کو تھامنے والا ہے۔ نہ اسے اونگھ آتی ہے نہ نیند۔ آسمانوں اور زمین میں جو کچھ ہے سب اسی کا ہے۔",
            englishTranslation = "Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth.",
            theme = "Ayat al-Kursi (Supreme Majesty of Allah)"
        ),
        QuranAyah(
            surahNumber = 94,
            surahNameArabic = "الشرح",
            surahNameEnglish = "Ash-Sharh",
            ayahNumber = 5,
            arabicText = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            urduTranslation = "پس بے شک ہر مشکل کے ساتھ آسانی ہے، بے شک ہر مشکل کے ساتھ آسانی ہے۔",
            englishTranslation = "For indeed, with hardship [will be] ease. Indeed, with hardship [will be] ease.",
            theme = "Hope, Relief and Perseverance"
        ),
        QuranAyah(
            surahNumber = 2,
            surahNameArabic = "البقرة",
            surahNameEnglish = "Al-Baqarah",
            ayahNumber = 186,
            arabicText = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
            urduTranslation = "اور جب میرے بندے آپ سے میرے متعلق سوال کریں تو فرما دیجیے کہ میں قریب ہوں۔ پکارنے والے کی دعا قبول کرتا ہوں جب وہ مجھے پکارے۔",
            englishTranslation = "And when My servants ask you concerning Me, indeed I am near. I respond to the invocation of the supplicant when he calls upon Me.",
            theme = "Nearness of Allah & Acceptance of Dua"
        ),
        QuranAyah(
            surahNumber = 55,
            surahNameArabic = "الرحمن",
            surahNameEnglish = "Ar-Rahman",
            ayahNumber = 13,
            arabicText = "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ",
            urduTranslation = "پس تم دونوں (انسان اور جن) اپنے رب کی کون کون سی نعمتوں کو جھٹلاؤ گے؟",
            englishTranslation = "So which of the favors of your Lord would you deny?",
            theme = "Gratitude for Divine Blessings"
        ),
        QuranAyah(
            surahNumber = 20,
            surahNameArabic = "طه",
            surahNameEnglish = "Taha",
            ayahNumber = 25,
            arabicText = "رَبِّ اشْرَحْ لِي صَدْرِي ۝ وَيَسِّرْ لِي أَمْرِي ۝ وَاحْلُلْ عُقْدَةً مِّن لِّسَانِي ۝ يَفْقَهُوا قَوْلِي",
            urduTranslation = "اے میرے پروردگار! میرے لیے میرا سینہ کھول دے، اور میرے کام کو میرے لیے آسان بنا دے، اور میری زبان کی گرہ کھول دے تاکہ وہ میری بات سمجھ سکیں۔",
            englishTranslation = "My Lord, expand for me my chest [with assurance] and ease for me my task and untie the knot from my tongue that they may understand my speech.",
            theme = "Dua of Prophet Musa (Confidence & Eloquence)"
        ),
        QuranAyah(
            surahNumber = 112,
            surahNameArabic = "الإخلاص",
            surahNameEnglish = "Al-Ikhlas",
            ayahNumber = 1,
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
            urduTranslation = "فرما دیجیے کہ وہ اللہ ایک ہے۔ اللہ بے نیاز ہے۔ نہ اس کی کوئی اولاد ہے اور نہ وہ کسی کی اولاد ہے۔ اور نہ کوئی اس کا ہمسر ہے۔",
            englishTranslation = "Say, 'He is Allah, [who is] One, Allah, the Eternal Refuge. He neither begets nor is born, nor is there to Him any equivalent.'",
            theme = "Tawhid (Absolute Oneness of Allah)"
        ),
        QuranAyah(
            surahNumber = 3,
            surahNameArabic = "آل عمران",
            surahNameEnglish = "Ali 'Imran",
            ayahNumber = 139,
            arabicText = "وَلَا تَهِنُوا وَلَا تَحْزَنُوا وَأَنتُمُ الْأَعْلَوْنَ إِن كُنتُم مُّؤْمِنِينَ",
            urduTranslation = "اور تم نہ سستی کرو اور نہ غمگین ہو، اور تم ہی غالب رہو گے اگر تم سچے مومن ہو۔",
            englishTranslation = "Do not weaken and do not grieve, and you will be superior if you are [true] believers.",
            theme = "Strength, Courage & Faith"
        )
    )

    val authenticDuas = listOf(
        IslamicDua(
            id = "dua_travel",
            titleUrdu = "سفر کی مسنون دعا",
            titleEnglish = "Dua for Travelling",
            category = DuaCategory.TRAVEL,
            arabicText = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ",
            transliteration = "Subhanalladhi sakh-khara lana hadha wa ma kunna lahu muqrinin, wa inna ila Rabbina lamunqalibun",
            urduTranslation = "پاک ہے وہ ذات جس نے اس (سواری) کو ہمارے قابو میں کر دیا حالانکہ ہم اس پر قابو پانے والے نہ تھے، اور بے شک ہم اپنے رب ہی کی طرف لوٹ کر جانے والے ہیں۔",
            englishTranslation = "Glory to Him who has subjected this to us, and we could never have it by our efforts. And indeed, to our Lord we will return.",
            authenticSource = "Sahih Muslim 1342"
        ),
        IslamicDua(
            id = "dua_protection",
            titleUrdu = "ہر بلا اور آفت سے حفاظت کی دعا",
            titleEnglish = "Protection From All Harm",
            category = DuaCategory.PROTECTION,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahilladhi la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim",
            urduTranslation = "اللہ کے نام کے ساتھ جس کے نام کی برکت سے زمین اور آسمان کی کوئی چیز نقصان نہیں پہنچا سکتی اور وہ خوب سننے والا اور جاننے والا ہے۔",
            englishTranslation = "In the name of Allah, with whose name nothing on earth or in heaven can cause harm, and He is the All-Hearing, All-Knowing.",
            authenticSource = "Sunan Abi Dawud 5088, Jami' at-Tirmidhi 3388"
        ),
        IslamicDua(
            id = "dua_distress",
            titleUrdu = "غم، پریشانی اور بے چینی دور کرنے کی دعا (دعا یونس)",
            titleEnglish = "Relief from Distress & Anxiety",
            category = DuaCategory.DISTRESS,
            arabicText = "لَا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
            transliteration = "La ilaha illa Anta subhanaka inni kuntu minaz-zalimin",
            urduTranslation = "تیرے سوا کوئی معبود نہیں، تو پاک ہے، بے شک میں ہی زیادتی کرنے والوں میں سے تھا۔",
            englishTranslation = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
            authenticSource = "Quran 21:87, Jami' at-Tirmidhi 3505"
        ),
        IslamicDua(
            id = "dua_morning",
            titleUrdu = "صبح کے وقت کی دعا",
            titleEnglish = "Morning Supplication (Azkar)",
            category = DuaCategory.MORNING_EVENING,
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            transliteration = "Asbahna wa asbahal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la sharika lah",
            urduTranslation = "ہم نے صبح کی اور اللہ کے لیے تمام بادشاہی نے صبح کی، اور تمام تعریف اللہ کے لیے ہے، اللہ کے سوا کوئی معبود نہیں وہ اکیلا ہے اس کا کوئی شریک نہیں۔",
            englishTranslation = "We have entered a new day, and the dominion belongs to Allah. All praise is due to Allah. None has the right to be worshipped but Allah alone, with no partner.",
            authenticSource = "Sahih Muslim 2723"
        ),
        IslamicDua(
            id = "dua_evening",
            titleUrdu = "شام کے وقت کی دعا",
            titleEnglish = "Evening Supplication (Azkar)",
            category = DuaCategory.MORNING_EVENING,
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            transliteration = "Amsayna wa amsal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la sharika lah",
            urduTranslation = "ہم نے شام کی اور اللہ کے لیے تمام بادشاہی نے شام کی، اور تمام تعریف اللہ کے لیے ہے، کوئی معبود نہیں مگر اکیلا اللہ جس کا کوئی شریک نہیں۔",
            englishTranslation = "We have reached the evening and with it the whole dominion belongs to Allah. All praise is due to Allah alone.",
            authenticSource = "Sahih Muslim 2723"
        ),
        IslamicDua(
            id = "dua_healing",
            titleUrdu = "بیماری سے شفاء کی نبوی دعا",
            titleEnglish = "Dua for Health and Healing",
            category = DuaCategory.HEALTH_SHIFA,
            arabicText = "اللَّهُمَّ رَبَّ النَّاسِ أَذْهِبِ الْبَأْسَ، اشْفِ أَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا",
            transliteration = "Allahumma Rabban-nas adhhibil-ba's, ishfi Antash-Shafi, la shifa'a illa shifa'uk, shifa'an la yughadiru saqama",
            urduTranslation = "اے اللہ، تمام انسانوں کے پالنے والے! تکلیف کو دور فرما، شفا عطا کر تو ہی شفا دینے والا ہے، تیری شفا کے سوا کوئی شفا نہیں، ایسی شفا عطا فرما جو کسی بیماری کو نہ چھوڑے۔",
            englishTranslation = "O Allah, Lord of mankind, remove the disease and bring about healing; You are the Healer, there is no healing except Your healing—a healing that leaves no illness behind.",
            authenticSource = "Sahih al-Bukhari 5743, Sahih Muslim 2191"
        ),
        IslamicDua(
            id = "dua_knowledge",
            titleUrdu = "علم اور فہم میں اضافے کی دعا",
            titleEnglish = "Dua for Knowledge & Wisdom",
            category = DuaCategory.KNOWLEDGE,
            arabicText = "رَّبِّ زِدْنِي عِلْمًا",
            transliteration = "Rabbi zidni 'ilma",
            urduTranslation = "اے میرے رب! میرے علم میں اضافہ فرما۔",
            englishTranslation = "My Lord, increase me in knowledge.",
            authenticSource = "Quran 20:114"
        ),
        IslamicDua(
            id = "dua_istighfar",
            titleUrdu = "سید الاستغفار (بخشش کی عظیم ترین دعا)",
            titleEnglish = "Sayyid al-Istighfar (Supreme Forgiveness)",
            category = DuaCategory.FORGIVENESS,
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'udhu bika min sharri ma sana'tu, abu'u laka bini'matika 'alayya wa abu'u bidhanbi faghfir li fa'innahu la yaghfirudh-dhunuba illa Anta",
            urduTranslation = "اے اللہ! تو میرا رب ہے، تیرے سوا کوئی معبود نہیں۔ تو نے مجھے پیدا کیا اور میں تیرا بندہ ہوں، اور میں اپنی استطاعت کے مطابق تیرے عہد اور وعدے پر قائم ہوں۔ میں اپنے کیے کے شر سے تیری پناہ مانگتا ہوں، اپنے اوپر تیری نعمتوں کا اعتراف کرتا ہوں اور اپنے گناہوں کا اقرار کرتا ہوں، پس مجھے بخش دے کیونکہ تیرے سوا کوئی گناہوں کو نہیں بخش سکتا۔",
            englishTranslation = "O Allah, You are my Lord, there is no god but You. You created me and I am Your servant, and I remain upon Your covenant and promise as much as I am able. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me and I acknowledge my sin, so forgive me, for none forgives sins except You.",
            authenticSource = "Sahih al-Bukhari 6306"
        )
    )

    // Calculate prayer times for coordinates (or default to Mecca coordinates if GPS not yet obtained)
    fun calculatePrayerTimes(latitude: Double = 21.4225, longitude: Double = 39.8262): List<PrayerTime> {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTimeInMinutes = currentHour * 60 + currentMinute

        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        // Astronomical solar calculations approximation
        val b = 2 * PI * (dayOfYear - 81) / 365
        val equationOfTime = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b)
        val solarDeclination = 23.45 * sin(b)

        val timeZoneOffset = (calendar.timeZone.rawOffset + calendar.timeZone.dstSavings) / (1000.0 * 60 * 60)
        val solarNoon = 12.0 + (timeZoneOffset - longitude / 15.0) - (equationOfTime / 60.0)

        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(solarDeclination)

        fun hourAngle(altitudeDeg: Double): Double {
            val altRad = Math.toRadians(altitudeDeg)
            val cosH = (sin(altRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
            return if (cosH > 1.0) 0.0 else if (cosH < -1.0) 180.0 else Math.toDegrees(acos(cosH))
        }

        val fajrAngle = hourAngle(-18.0)
        val sunriseAngle = hourAngle(-0.833)
        val maghribAngle = hourAngle(-0.833)
        val ishaAngle = hourAngle(-18.0)

        val fajrHours = solarNoon - (fajrAngle / 15.0)
        val sunriseHours = solarNoon - (sunriseAngle / 15.0)
        val dhuhrHours = solarNoon + (2.0 / 60.0)
        // Asr Hanafi/Shafi'i mid approximation
        val asrAltitude = Math.toDegrees(atan(1.0 / (1.0 + tan(abs(latRad - decRad)))))
        val asrAngle = hourAngle(asrAltitude)
        val asrHours = solarNoon + (asrAngle / 15.0)
        val maghribHours = solarNoon + (maghribAngle / 15.0)
        val ishaHours = solarNoon + (ishaAngle / 15.0)

        fun formatTime(hoursDec: Double): Pair<Int, Int> {
            var h = hoursDec.toInt()
            var m = ((hoursDec - h) * 60).toInt()
            if (m >= 60) {
                h += 1
                m -= 60
            }
            if (h >= 24) h -= 24
            if (h < 0) h += 24
            return Pair(h, m)
        }

        val timesRaw = listOf(
            Triple("Fajr", "فجر", formatTime(fajrHours)),
            Triple("Sunrise", "طلوع آفتاب", formatTime(sunriseHours)),
            Triple("Dhuhr", "ظہر", formatTime(dhuhrHours)),
            Triple("Asr", "عصر", formatTime(asrHours)),
            Triple("Maghrib", "مغرب", formatTime(maghribHours)),
            Triple("Isha", "عشاء", formatTime(ishaHours))
        )

        var foundNext = false
        val list = mutableListOf<PrayerTime>()

        for ((eng, urdu, hm) in timesRaw) {
            val totalMins = hm.first * 60 + hm.second
            val diff = totalMins - currentTimeInMinutes
            val isNext = !foundNext && diff > 0
            if (isNext) foundNext = true

            val minsRemaining = if (diff > 0) diff.toLong() else (1440 + diff).toLong()
            val amPm = if (hm.first >= 12) "PM" else "AM"
            val displayHour = if (hm.first % 12 == 0) 12 else hm.first % 12
            val formatted = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, hm.second, amPm)

            list.add(
                PrayerTime(
                    nameEnglish = eng,
                    nameUrdu = urdu,
                    timeFormatted = formatted,
                    hour24 = hm.first,
                    minute = hm.second,
                    isNext = isNext,
                    minutesRemaining = minsRemaining
                )
            )
        }

        // If all prayers passed today, next is Fajr tomorrow
        if (!foundNext && list.isNotEmpty()) {
            val fajr = list[0]
            val totalMins = fajr.hour24 * 60 + fajr.minute
            val remaining = (1440 - currentTimeInMinutes) + totalMins
            list[0] = fajr.copy(isNext = true, minutesRemaining = remaining.toLong())
        }

        return list
    }

    // Great-Circle azimuth to the Holy Kaaba in Mecca (21.4225 N, 39.8262 E)
    fun calculateQiblaDirection(userLat: Double, userLng: Double): Float {
        val kaabaLat = Math.toRadians(21.4225)
        val kaabaLng = Math.toRadians(39.8262)
        val userLatRad = Math.toRadians(userLat)
        val userLngRad = Math.toRadians(userLng)

        val deltaLng = kaabaLng - userLngRad
        val y = sin(deltaLng) * cos(kaabaLat)
        val x = cos(userLatRad) * sin(kaabaLat) - sin(userLatRad) * cos(kaabaLat) * cos(deltaLng)
        val qiblaRad = atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad).toFloat()
        if (qiblaDeg < 0) {
            qiblaDeg += 360f
        }
        return qiblaDeg
    }
}
