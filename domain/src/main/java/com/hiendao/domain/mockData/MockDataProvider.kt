package com.hiendao.domain.mockData

import com.hiendao.domain.map.toReaderItems
import com.hiendao.domain.model.*


object MockDataProvider {

    fun getMockBooks(): List<Book> = listOf(
        Book(
            id = "book_1",
            title = "Truyện Kiều",
            author = "Nguyễn Du",
            description = "Tác phẩm kinh điển của văn học Việt Nam",
            totalChapters = 3,
            coverImageUrl = "https://example.com/cover1.jpg"
        ),
        Book(
            id = "book_2",
            title = "Chí Phèo",
            author = "Nam Cao",
            description = "Tác phẩm nổi tiếng về cuộc sống nông thôn",
            totalChapters = 1,
            coverImageUrl = "https://example.com/cover2.jpg"
        )
    )

    fun getMockChapters(bookId: String): List<Chapter> = when (bookId) {
        "book_1" -> listOf(
            Chapter(
                id = "chapter_1_1",
                bookId = bookId,
                title = "Chương 1: Gặp gỡ",
                content = generateLongText(),
                order = 1,
                wordCount = 1500
            ),
            Chapter(
                id = "chapter_1_2",
                bookId = bookId,
                title = "Chương 2: Tình yêu",
                content = generateLongText(),
                order = 2,
                wordCount = 1800
            ),
            Chapter(
                id = "chapter_1_3",
                bookId = bookId,
                title = "Chương 3: Kết thúc",
                content = generateLongText(),
                order = 3,
                wordCount = 2000
            )
        )
        "book_2" -> listOf(
            Chapter(
                id = "chapter_2_1",
                bookId = bookId,
                title = "Chí Phèo",
                content = generateLongText(),
                order = 1,
                wordCount = 3000
            )
        )
        else -> emptyList()
    }

    fun getMockReaderItems(chapterId: String): List<ReaderItem> {
            val bookId = when {
            chapterId.startsWith("chapter_1_") -> "book_1"
            chapterId.startsWith("chapter_2_") -> "book_2"
            else -> return emptyList()
        }
            val chapter = getMockChapters(bookId).firstOrNull { it.id == chapterId } ?: return emptyList()
        return chapter.toReaderItems()
    }

    private fun generateLongText(): String {
        return """
        Ngày xửa ngày xưa, trong một ngôi làng nhỏ nằm ven dòng sông lặng lẽ, có một chàng trai tên là An. 
        An sinh ra trong một gia đình nghèo, nhưng trong lòng luôn mang một ước mơ lớn lao — được đi xa, được khám phá thế giới bên ngoài, nơi mà cậu chỉ từng nghe qua lời kể của những người khách bộ hành.

        Mỗi buổi sáng, khi mặt trời còn chưa ló dạng, An đã ra bến sông, ngồi bên bờ cát mịn, nhìn những con thuyền chở hàng xuôi ngược. 
        Cậu tưởng tượng rằng một ngày nào đó, chính mình sẽ là người chèo lái con thuyền đi qua những vùng đất xa xôi, nghe những ngôn ngữ lạ, ngắm những ngọn núi phủ tuyết trắng xóa.

        Cha của An là một người thợ rèn, tay chai sần, lưng còng vì năm tháng. 
        Ông từng bảo: “Thế giới ngoài kia rộng lớn, nhưng cũng đầy hiểm nguy. 
        Ở lại đây, làm ruộng, rèn sắt, cưới vợ, sinh con — đó mới là cuộc sống thật sự.” 
        An chỉ cười, nhưng trong lòng, cậu biết mình không thể sống mãi ở nơi này.

        Một hôm, khi gió mùa về mang theo mùi hương của đất ẩm, một đoàn thương nhân dừng lại ở làng. 
        Họ kể về những thành phố xa xôi, nơi có đèn lồng sáng rực cả đêm, nơi con người từ khắp phương trời tụ họp. 
        Từ hôm đó, An không ngủ được. 
        Cậu cảm thấy tiếng gọi mơ hồ của những con đường chưa từng đi qua.

        Nhiều năm sau, khi đã đủ tuổi, An quyết định rời làng. 
        Mẹ cậu khóc, cha im lặng. 
        Chỉ có cánh đồng lúa đón gió chiều là chứng kiến bước chân đầu tiên của cậu trên hành trình dài.

        --- 

        Hành trình của An không hề dễ dàng. 
        Cậu đi qua những cánh rừng rậm rạp, nơi ánh nắng không thể xuyên qua tán lá. 
        Cậu vượt qua những con đèo phủ sương trắng, lạnh đến tê buốt. 
        Cậu từng đói, từng ngã, từng nghĩ đến việc quay về — nhưng mỗi khi nhìn thấy bình minh, cậu lại tiếp tục.

        Một ngày nọ, An đến một thị trấn bên bờ biển. 
        Ở đó, cậu gặp một ông lão bán sách cũ. 
        Ông lão nhìn An, mỉm cười và nói: 
        “Cậu đi tìm thế giới, nhưng có lẽ, thế giới lại đang tìm chính cậu.” 
        Câu nói ấy theo An suốt nhiều năm sau.

        Cậu làm thuê trên tàu đánh cá, học cách buộc dây, giăng lưới, điều khiển buồm. 
        Cậu quen biết với những người thủy thủ đến từ nhiều quốc gia — người kể về sa mạc mênh mông, người kể về thành phố ngập ánh đèn, người kể về quê hương bị tuyết phủ quanh năm.

        Mỗi câu chuyện lại làm ước mơ của An lớn hơn một chút.

        ---

        Một buổi sáng mùa thu, khi biển tĩnh lặng như tấm gương, con tàu mà An làm việc bị mắc cạn ở một đảo lạ. 
        Trên đảo có một ngôi làng nhỏ, người dân nói ngôn ngữ lạ, nhưng ánh mắt họ hiền hậu. 
        Cậu được một cô gái tên Linh chăm sóc trong suốt thời gian ở đó. 
        Linh có giọng nói nhẹ như gió, và nụ cười khiến An quên đi mọi mỏi mệt.

        Họ đi dạo trên bãi cát, nói chuyện bằng những cử chỉ, bằng nụ cười, bằng sự lặng im. 
        Lần đầu tiên trong đời, An thấy bình yên hơn cả những giấc mơ của mình. 
        Nhưng rồi ngày con tàu được sửa xong cũng đến. 
        Cậu phải rời đi.

        Linh tặng An một chiếc vòng tay bằng vỏ sò, nói bằng giọng run run:
        “Nếu anh còn nhớ em, hãy quay lại khi hoa biển nở.”

        ---

        Nhiều năm sau, An đã trở thành một người thủy thủ dày dạn. 
        Cậu đi qua hàng chục vùng biển, ghi chép lại câu chuyện của mình thành những trang giấy. 
        Nhưng dù đi đâu, mỗi khi đêm về, khi gió biển thổi vào mặt, cậu vẫn đưa tay chạm vào chiếc vòng tay ấy — giờ đã sờn, nhưng vẫn nguyên vẹn mùi muối của biển và ký ức của một ngày xưa.

        Người ta kể rằng, có lần, người ta thấy một con tàu neo lại gần đảo ấy, và một người đàn ông trẻ đứng nhìn vào bờ, trong tay cầm một bó hoa lạ — loài hoa chỉ nở duy nhất khi mùa biển đổi màu.

        Có lẽ, An đã trở về.

        ---

        Đây là một đoạn truyện dài được tạo ra để mô phỏng nội dung đọc thật trong ứng dụng đọc truyện.
        Mục tiêu của nó là giúp kiểm tra:
        1. Việc chia nhỏ nội dung thành các ReaderTextItem có hoạt động đúng không.
        2. LazyColumn có giữ hiệu suất ổn định khi render nhiều đoạn dài không.
        3. Việc tính phần trăm đọc (progress) có chính xác khi scroll qua các item.
        4. Các tính năng như highlight, voice reading (TTS), hoặc save position có mượt mà không.

        Kết thúc chương.
    """.trimIndent()
    }

    fun getDefaultReadingSettings(): ReadingSettings = ReadingSettings()

    fun getDefaultReadingProgress(bookId: String, chapterId: String): ReadingProgress =
        ReadingProgress(
            bookId = bookId,
            chapterId = chapterId,
            itemIndex = 0,
            scrollOffset = 0,
            totalItems = getMockReaderItems(chapterId).size
        )
}
