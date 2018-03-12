package consulting.vectr.model

case class ParliamentaryQuestionSmallSummary(
                                              author: String,
                                              party: String,
                                              topic: String,
                                              department_long: String,
                                              department: String,
                                              date: String,
                                              questionCount: Int = 1
                                            )
