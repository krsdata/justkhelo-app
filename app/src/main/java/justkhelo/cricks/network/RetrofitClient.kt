package justkhelo.cricks.network

import android.content.Context
import justkhelo.cricks.BuildConfig
import justkhelo.cricks.NinjaApplication
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.MyPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(val context: Context) {
    private lateinit var interceptor: HttpLoggingInterceptor
    private lateinit var okHttpClient: OkHttpClient
    private var retrofit: Retrofit? = null

    val client: Retrofit
        get() {

            interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            if (BuildConfig.DEBUG) {
                okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(Interceptor { chain ->
                        val original = chain.request()
                        val builder = original.newBuilder()
                        builder.addHeader("Accept", "application/json")
                            .addHeader("Authorization", "Bearer " + MyPreferences.getToken(context))
                            .addHeader(
                                "version_code",
                                NinjaApplication.appVersion.toString()
                            )
                        val request = builder.build()

                        BindingUtils.logD(
                            "ServiceGen",
                            "headrs: " + request.headers.toString()
                        )
                        chain.proceed(request)
                    })
                    /*.connectionSpecs(
                        Arrays.asList(
                            ConnectionSpec.MODERN_TLS,
                            ConnectionSpec.CLEARTEXT
                        )
                    )
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .retryOnConnectionFailure(true)*/
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .cache(null)
                    .build()
            } else {
                okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(Interceptor { chain ->
                        val original = chain.request()
                        val builder = original.newBuilder()
                        builder.addHeader("Accept", "application/json")
                            .addHeader("Authorization", "Bearer " + MyPreferences.getToken(context))
                            .addHeader(
                                "version_code",
                                NinjaApplication.appVersion.toString()
                            )
                        val request = builder.build()
                        chain.proceed(request)
                    })
                    /*.connectionSpecs(
                        Arrays.asList(
                            ConnectionSpec.MODERN_TLS,
                            ConnectionSpec.CLEARTEXT
                        )
                    )
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .retryOnConnectionFailure(true)*/
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .cache(null)
                    .build()
            }
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BindingUtils.BASE_URL_API)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
            }
            return retrofit!!
        }
}