package consulting.vectr.model

case class ParliamentaryQuestionWeb(
                                     link: String,
                                     id: Int,
                                     status: String, //enum possible
                                     legislation: Int,
                                     sdocname: String,
                                     document_id: String,
                                     document_date: Option[String],
                                     author: String,
                                     author_party: String,
                                     language: String, //enum possible
                                     department_number: Int,
                                     department_name_nl: String,
                                     department_name_fr: String,
                                     sub_department_name_nl: Option[String],
                                     sub_department_name_fr: Option[String],
                                     question_number: Int,
                                     title_nl: String,
                                     title_fr: String,
                                     question_text_nl: Option[String],
                                     question_text_fr: Option[String],
                                     answer_text_nl: Option[String],
                                     answer_text_fr: Option[String],
                                     subject_nl: Option[String],
                                     subject_fr: Option[String],
                                     sub_subject_nl: List[String],
                                     sub_subject_fr: List[String]
                                   )
