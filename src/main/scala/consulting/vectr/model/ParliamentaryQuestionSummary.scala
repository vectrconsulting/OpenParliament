package consulting.vectr.model

case class ParliamentaryQuestionSummary(
                                         author: String,
                                         party: String,
                                         topic: String,
                                         department_long: String,
                                         department: String,
                                         date: String,
                                         title: String,
                                         question: String,
                                         answer: String,
                                         questionCount: Int
                                       )
