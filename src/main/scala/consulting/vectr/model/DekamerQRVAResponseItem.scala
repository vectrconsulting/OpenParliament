package consulting.vectr.model

case class DekamerQRVAResponseItem(
                                    link: DekamerQRVAResponseItemsLink,
                                    SDOCNAME: String,
                                    TITF: Option[String],
                                    TITN: Option[String],
                                    AUT: Option[String],
                                    MAINTHESAF: Option[String],
                                    MAINTHESAN: Option[String],
                                    STATUSQ: Option[String],
                                    DEPTF: Option[String],
                                    DEPTN: Option[String]
                                  )
