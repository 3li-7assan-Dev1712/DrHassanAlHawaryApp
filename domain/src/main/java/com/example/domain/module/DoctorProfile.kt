package com.example.domain.module

data class DoctorProfile(
    val name: String,
    val title: String,
    val bio: String,
    val profileImageUrl: String,
    val education: List<String>,
    val achievements: List<String>,
    val socialLinks: SocialLinks
)

data class SocialLinks(
    val facebook: String,
    val youtube: String,
    val whatsapp: String,
    val email: String
)

val fakeDoctorProfile = DoctorProfile(
    name = "د. حسان الحواري",
    title = "أستاذ التفسير وعلوم القرآن",
    bio = "د. حسان الحواري هو باحث وأكاديمي متخصص في الدراسات الإسلامية، قضى أكثر من ٢٠ عاماً في تدريس علوم القرآن وتفسيره. له العديد من المحاضرات والدروس العلمية التي تهدف إلى تبسيط مفاهيم القرآن الكريم للشباب المسلم.",
    profileImageUrl = "https://your-placeholder-image-url.com/dr_hassan.jpg",
    education = listOf(
        "دكتوراة في التفسير وعلوم القرآن - جامعة الأزهر",
        "ماجستير في الدراسات الإسلامية",
        "إجازة في القراءات العشر"
    ),
    achievements = listOf(
        "مؤلف كتاب 'تأملات في سورة الكهف'",
        "مؤسس أكاديمية علوم القرآن الرقمية",
        "أكثر من ٥٠٠ ساعة صوتية ومرئية في تفسير القرآن"
    ),
    socialLinks = SocialLinks(
        facebook = "https://facebook.com",
        youtube = "https://youtube.com",
        whatsapp = "https://wa.me/123456789",
        email = "dr.hassan@example.com"
    )
)