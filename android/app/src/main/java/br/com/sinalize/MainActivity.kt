package br.com.sinalize

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sinalize.ui.theme.*

private enum class Page { HOME, CATEGORIES, QUIZ, RESULT }
private data class DemoCategory(val name: String, val symbol: String, val description: String)

// Dados ilustrativos da interface: integração REST será realizada na próxima etapa.
private val demoCategories = listOf(
    DemoCategory("Saudações", "👋", "Cumprimentos do dia a dia"),
    DemoCategory("Animais", "🐾", "Nomes de animais"),
    DemoCategory("Alimentos", "🍎", "Comidas e bebidas"),
    DemoCategory("Cores", "🎨", "Cores mais utilizadas"),
    DemoCategory("Família", "🏠", "Pessoas e relações"),
    DemoCategory("Números", "🔢", "Contagem básica")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SinalizeTheme { SinalizeApp() } }
    }
}

@Composable
private fun SinalizeApp() {
    var page by remember { mutableStateOf(Page.HOME) }
    var category by remember { mutableStateOf(demoCategories.first().name) }
    var currentQuestion by remember { mutableIntStateOf(1) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf(demoCategories.first().name) }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = page != Page.HOME) {
        if (page == Page.QUIZ) showExitDialog = true
        else page = if (page == Page.RESULT) Page.CATEGORIES else Page.HOME
    }
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Sair da demonstração?") },
            text = { Text("Você retornará à seleção de categorias.") },
            confirmButton = { TextButton(onClick = { showExitDialog = false; page = Page.CATEGORIES }) { Text("Sair") } },
            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text("Continuar") } }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Background) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            TopBrand(onHome = { page = Page.HOME })
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                when (page) {
                    Page.HOME -> HomePage(onStart = { page = Page.CATEGORIES })
                    Page.CATEGORIES -> CategoriesPage(
                        selection = selectedCategory,
                        onSelect = { selectedCategory = it },
                        onStart = {
                            category = selectedCategory
                            currentQuestion = 1
                            selectedAnswer = null
                            page = Page.QUIZ
                        }
                    )
                    Page.QUIZ -> QuizPage(
                        category = category,
                        number = currentQuestion,
                        answer = selectedAnswer,
                        onAnswer = { selectedAnswer = it },
                        onNext = {
                            if (currentQuestion == 5) page = Page.RESULT
                            else { currentQuestion++; selectedAnswer = null }
                        }
                    )
                    Page.RESULT -> ResultPage(
                        onRepeat = { currentQuestion = 1; selectedAnswer = null; page = Page.QUIZ },
                        onCategories = { page = Page.CATEGORIES }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBrand(onHome: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White)
            .clickable(onClick = onHome).padding(horizontal = 22.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(42.dp).background(PaleTeal, RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) { Text("✋", fontSize = 23.sp) }
        Text("sinalize.", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Navy)
        Spacer(Modifier.weight(1f))
        Text("DEMO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Teal)
    }
}

@Composable
private fun HomePage(onStart: () -> Unit) {
    Spacer(Modifier.height(16.dp))
    Text("APRENDER • PRATICAR • EVOLUIR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Teal)
    Text("Cada sinal é uma nova conexão.", fontSize = 32.sp, lineHeight = 39.sp, fontWeight = FontWeight.Bold, color = Navy)
    Text("Explore a Língua Brasileira de Sinais no seu ritmo, com atividades simples e interativas.", fontSize = 16.sp, lineHeight = 24.sp, color = Muted)
    Card(
        modifier = Modifier.fillMaxWidth().height(196.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PaleTeal)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🤟", fontSize = 64.sp)
            Spacer(Modifier.height(8.dp))
            Text("Seu aprendizado começa aqui", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Navy)
        }
    }
    PrimaryAction("Começar a aprender", onStart)
    Text("Sem cadastro • No seu ritmo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 13.sp, color = Muted)
}

@Composable
private fun CategoriesPage(selection: String, onSelect: (String) -> Unit, onStart: () -> Unit) {
    SectionHeading("ESCOLHA UM TEMA", "O que vamos aprender?", "Selecione uma categoria para praticar.")
    demoCategories.forEach { item ->
        val active = selection == item.name
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onSelect(item.name) },
            border = BorderStroke(1.dp, if (active) Teal else OutlineSoft),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = if (active) PaleTeal else Color.White)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(item.symbol, fontSize = 27.sp)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                    Text(item.description, fontSize = 13.sp, color = Muted)
                }
                RadioButton(selected = active, onClick = { onSelect(item.name) }, colors = RadioButtonDefaults.colors(selectedColor = Teal))
            }
        }
    }
    PrimaryAction("Iniciar demonstração", onStart)
    DemoNotice("As categorias ainda são dados de demonstração. Na próxima etapa, virão de GET /categorias.")
}

@Composable
private fun QuizPage(category: String, number: Int, answer: String?, onAnswer: (String) -> Unit, onNext: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(category, fontSize = 14.sp, color = Muted)
        Text("$number / 5", fontWeight = FontWeight.Bold, color = Navy)
    }
    LinearProgressIndicator(
        progress = { number / 5f },
        modifier = Modifier.fillMaxWidth().height(8.dp),
        color = Teal,
        trackColor = PaleTeal
    )
    Text("Qual palavra corresponde a este sinal?", fontSize = 23.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold, color = Navy)
    Card(
        modifier = Modifier.fillMaxWidth().height(205.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PaleTeal)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("▶", fontSize = 45.sp, color = Teal)
            Spacer(Modifier.height(12.dp))
            Text("Área reservada para o sinal em Libras", textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold, color = Navy)
            Text("Vídeo ou avatar ainda não integrado", fontSize = 12.sp, color = Muted)
        }
    }
    listOf("Olá", "Obrigado", "Bom dia", "Tchau").forEachIndexed { index, option ->
        val active = option == answer
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onAnswer(option) },
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (active) Teal else OutlineSoft),
            colors = CardDefaults.cardColors(containerColor = if (active) PaleTeal else Color.White)
        ) {
            Row(modifier = Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${'A' + index}", color = Teal, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(16.dp))
                Text(option, color = Navy, modifier = Modifier.weight(1f))
                if (active) Text("✓", color = Teal, fontWeight = FontWeight.Bold)
            }
        }
    }
    PrimaryAction(if (number == 5) "Ver final da demonstração" else "Próxima pergunta", onNext, enabled = answer != null)
    DemoNotice("Protótipo visual: as opções não representam um sinal real e não há correção ou envio de respostas à API.")
}

@Composable
private fun ResultPage(onRepeat: () -> Unit, onCategories: () -> Unit) {
    Spacer(Modifier.height(38.dp))
    Box(
        modifier = Modifier.size(92.dp)
            .background(PaleTeal, CircleShape), contentAlignment = Alignment.Center
    ) { Text("★", fontSize = 54.sp, color = Teal) }
    Text("Fim da demonstração!", modifier = Modifier.fillMaxWidth(), fontSize = 27.sp,
        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Navy)
    Text("Você navegou pelas cinco perguntas do protótipo.",
        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Muted)
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Resultado real ainda não disponível", fontWeight = FontWeight.Bold, color = Navy)
            Spacer(Modifier.height(8.dp))
            Text("O placar será calculado quando os sinais e o registro de respostas estiverem integrados.",
                textAlign = TextAlign.Center, color = Muted, fontSize = 14.sp)
        }
    }
    Spacer(Modifier.height(16.dp))
    PrimaryAction("Repetir demonstração", onRepeat)
    OutlinedButton(onClick = onCategories, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(14.dp)) { Text("Escolher outra categoria") }
}

@Composable
private fun SectionHeading(label: String, title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Teal)
        Text(title, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text(description, fontSize = 15.sp, color = Muted)
    }
}

@Composable
private fun PrimaryAction(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(14.dp)) { Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun DemoNotice(message: String) {
    Text(message, fontSize = 12.sp, lineHeight = 18.sp, color = Muted)
}
