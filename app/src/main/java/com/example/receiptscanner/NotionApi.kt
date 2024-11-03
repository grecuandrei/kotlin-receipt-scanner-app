import com.example.receiptscanner.BuildConfig
import com.example.receiptscanner.first.PageRequest
import com.example.receiptscanner.first.QueryDatabaseRequest
import com.example.receiptscanner.first.QueryDatabaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

// Define Notion API interface
interface NotionApi {
    @Headers(
        "Content-Type: application/json",
        "Notion-Version: 2022-06-28",
    )
    @POST("v1/pages")
    suspend fun createPage(@Header("Authorization") authHeader: String, @Body pageRequest: PageRequest): Response<Unit>

    @Headers(
        "Content-Type: application/json",
        "Notion-Version: 2022-06-28",
    )
    @POST("v1/databases/{database_id}/query")
    suspend fun queryDatabase(
        @Header("Authorization") authHeader: String,
        @Path("database_id") databaseId: String,
        @Body request: QueryDatabaseRequest = QueryDatabaseRequest() // Empty body for full query
    ): Response<QueryDatabaseResponse>
}
