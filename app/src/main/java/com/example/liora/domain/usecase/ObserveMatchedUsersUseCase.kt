package com.example.liora.domain.usecase

import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.repository.InteractionRepository
import com.example.liora.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map // Importar map é crucial aqui

/**
 * Use Case responsável por observar os perfis de usuários com quem o usuário atual teve um match mútuo.
 * Ele orquestra dados de dois repositórios:
 * 1. InteractionRepository para obter os IDs dos usuários com match E o chatId associado.
 * 2. UserRepository para obter os perfis completos desses usuários.
 */
class ObserveMatchedUsersUseCase(
    private val interactionRepository: InteractionRepository,
    private val userRepository: UserRepository
) {
    /**
     * Invoca o Use Case para começar a observar os usuários com match mútuo e seus respectivos chatIds.
     *
     * @param currentUserId O ID do usuário logado.
     * @return Um [Flow] de uma lista de [Pair]<UserProfile, String>, onde o primeiro elemento
     * é o perfil do usuário com match e o segundo [String] é o `chatId` correspondente a esse match.
     */
    operator fun invoke(currentUserId: String): Flow<List<Pair<UserProfile, String>>> {
        // 1. Observa os IDs dos usuários com match mútuo e os chatIds correspondentes em tempo real.
        // flatMapLatest é usado para que, se a lista de IDs/chatIds de matches mudar,
        // a observação dos perfis seja reiniciada com a nova lista, cancelando a anterior.
        return interactionRepository.observeMutualMatches(currentUserId)
            .flatMapLatest { matchedIdsAndChatIds -> // Agora este é um List<Pair<String (UID), String (ChatID)>>
                if (matchedIdsAndChatIds.isEmpty()) {
                    // Se não houver IDs de matches, emite um Flow de lista vazia imediatamente.
                    flowOf(emptyList())
                } else {
                    // Extrai apenas os UIDs para buscar os perfis no UserRepository.
                    val matchedUserIds = matchedIdsAndChatIds.map { it.first }
                    // Cria um mapa para associar rapidamente um UID ao seu chatId correspondente.
                    val chatIdMap = matchedIdsAndChatIds.toMap()

                    // 2. Divide os IDs dos usuários em "chunks" de no máximo 10.
                    // Isso é essencial para contornar a limitação de 10 elementos do 'whereIn' do Firestore.
                    val chunkedIds = matchedUserIds.chunked(10)

                    // 3. Para cada chunk, cria um Flow que observa os UserProfiles correspondentes.
                    val flowsOfProfilesPerChunk = chunkedIds.map { idChunk ->
                        userRepository.observeUserProfilesByIds(idChunk)
                    }

                    // 4. Combina todos os Flows de perfis de cada chunk em um único Flow.
                    // O operador `combine` é usado para emitir uma nova lista combinada
                    // sempre que qualquer um dos Flows subjacentes (para cada chunk) emitir um novo valor.
                    @Suppress("UNCHECKED_CAST") // O cast é seguro pois sabemos que são List<UserProfile>
                    combine(flowsOfProfilesPerChunk as List<Flow<List<UserProfile>>>) { combinedChunksArray ->
                        // Achata o Array de List<UserProfile> em uma única List<UserProfile>.
                        combinedChunksArray.flatMap { it }
                    }.map { userProfiles ->
                        // 5. Mapeia cada UserProfile para um Pair com seu chatId correspondente.
                        userProfiles.mapNotNull { profile ->
                            // Busca o chatId no mapa usando o UID do perfil.
                            // Se o chatId for encontrado (não nulo), cria o Pair.
                            chatIdMap[profile.uid]?.let { chatId ->
                                Pair(profile, chatId)
                            }
                        }
                    }
                }
            }
    }
}