package consulting.vectr.model

case class ParliamentaryQuestionPath(
                                      author: String,
                                      departement: String,
                                      party: String,
                                      topic: String,
                                      question_count: Int = 1
                                    )
