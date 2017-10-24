package consulting.vectr.model

case class DekamerQRVAResponse(disclaimer: String, start: Int, total: Int, items: List[DekamerQRVAResponseItem])

case class DekamerQRVAResponseItem(link: DekamerQRVAResponseItemsLink,
                                   SDOCNAME: String,
                                   TITF: Option[String],
                                   TITN: Option[String],
                                   AUT: Option[String],
                                   MAINTHESAF: Option[String],
                                   MAINTHESAN: Option[String],
                                   STATUSQ: Option[String],
                                   DEPTF: Option[String],
                                   DEPTN: Option[String])

case class DekamerQRVAResponseItemsLink(href: String, rel: String)

case class DekamerQRVAIdResponse(disclaimer: String, items: List[DekamerQRVAIdResponseItem])

case class DekamerQRVAIdResponseItem(link: DekamerQRVAResponseItemsLink,
                                     ID: Int,
                                     STATUSQ: Option[String],
                                     LEGISL: Int,
                                     DOCNAME: Option[String],
                                     DEPOTDAT: Option[String],
                                     AUT: Option[String],
                                     LANG: Option[String],
                                     DEPTPRES: Int,
                                     DEPTNUM: Int,
                                     SUBDEPTN: Option[String],
                                     SUBDEPTF: Option[String],
                                     DEPTN: Option[String],
                                     DEPTF: Option[String],
                                     QUESTNUM: Int,
                                     DELAIDAT: Option[String],
                                     TITN: Option[String],
                                     TITF: Option[String],
                                     STATUS_OL: Option[String],
                                     TEXTQN: Option[DekamerQRVAIdResponseItemText],
                                     STATUS_SL: Option[String],
                                     TEXTQF: Option[DekamerQRVAIdResponseItemText],
                                     PUBLICA1: Option[String],
                                     STATUSA1: Option[String],
                                     NUMA1: Int,
                                     CASA1: Option[String],
                                     TEXTA1F: Option[DekamerQRVAIdResponseItemText],
                                     TEXTA1N: Option[DekamerQRVAIdResponseItemText],
                                     PUBLICA2: Option[String],
                                     STATUSA2: Option[String],
                                     NUMA2: Int,
                                     CASA2: Option[String],
                                     TEXTA2F: Option[DekamerQRVAIdResponseItemText],
                                     TEXTA2N: Option[DekamerQRVAIdResponseItemText],
                                     PUBLICA3: Option[String],
                                     STATUSA3: Option[String],
                                     NUMA3: Int,
                                     CASA3: Option[String],
                                     TEXTA3F: Option[DekamerQRVAIdResponseItemText],
                                     TEXTA3N: Option[DekamerQRVAIdResponseItemText],
                                     PUBLICA4: Option[String],
                                     STATUSA4: Option[String],
                                     NUMA4: Int,
                                     CASA4: Option[String],
                                     TEXTA4F: Option[DekamerQRVAIdResponseItemText],
                                     TEXTA4N: Option[DekamerQRVAIdResponseItemText],
                                     MAIN_THESAF: Option[String],
                                     MAIN_THESAN: Option[String],
                                     MAIN_THESAD: Option[String],
                                     THESAF: List[String],
                                     THESAN: List[String],
                                     THESAD: List[String],
                                     DESCF: Option[String],
                                     DESCN: Option[String],
                                     DESCD: Option[String],
                                     FREEF: Option[String],
                                     FREEN: Option[String],
                                     FREED: Option[String])

case class DekamerQRVAIdResponseItemText(br: List[String])

case class ParliamentaryQuestionWeb(link: String,
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

case class WitAIResponse(msg_id: String,
                         _text: String,
                           entities: Map[String, List[WitAIEntity]]
                        )

case class WitAIEntity(confidence: Float,
                       value: String
                      )

case class DialogFlowResponse(sessionId: String,
                              result: DialogFlowResult
                             )

case class DialogFlowResult(resolvedQuery: String,
                            parameters: Map[String, List[String]]
                           )

case class Entity(name: String,
                  entries: List[EntityValue]
                 )

case class EntityValue(value: String,
                       synonyms: List[String]
                      )