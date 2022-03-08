package com.github.brugapp.brug


//@Module
//@TestInstallIn(
//    components = [ActivityComponent::class],
//    replaces = [SignInAccountModule::class]
//)
//object FakeSignInAccountModule {
//
//    @ActivityScoped
//    @Provides
//    fun provideConstantSignInAccount(@ApplicationContext context: Context): SignInAccount {
//        return FakeSignInAccount()
//    }
//}
//
//class FakeSignInAccount : SignInAccount() {
//
//    override val displayName: String
//        get() = "Son Goku"
//    override val idToken: String
//        get() = "0"
//    override val email: String
//        get() = "goku@capsulecorp.com"
//}
