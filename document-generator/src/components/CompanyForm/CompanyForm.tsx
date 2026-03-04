import { Form, Input, Select, DatePicker } from "antd"
import { Wrapper, TwoColumns, SectionTitle } from "./styles"

const { Option } = Select

export const CompanyForm = () => {
  return (
    <Wrapper>
      <SectionTitle>Rekvizitai</SectionTitle>

      <TwoColumns>
        <Form.Item label="Įmonės tipas" name="companyType">
          <Select>
            <Option value="small">Mažoji bendrija</Option>
            <Option value="uab">UAB</Option>
          </Select>
        </Form.Item>

        <Form.Item label="Įmonės pavadinimas" name="companyName">
          <Input />
        </Form.Item>

        <Form.Item label="Įmonės kategorija" name="companyCategory">
          <Input />
        </Form.Item>

        <Form.Item label="Įmonės kodas" name="companyCode">
          <Input />
        </Form.Item>

        <Form.Item label="Adresas" name="address">
          <Input />
        </Form.Item>

        <Form.Item label="Miestas" name="city">
          <Input />
        </Form.Item>

        <Form.Item label="Vadovo tipas" name="managerType">
          <Select>
            <Option value="director">Vadovas</Option>
          </Select>
        </Form.Item>

        <Form.Item label="Vadovo vardas ir pavardė" name="managerName">
          <Input />
        </Form.Item>

        <Form.Item label="Data" name="date">
          <DatePicker style={{ width: "100%" }} />
        </Form.Item>
      </TwoColumns>
    </Wrapper>
  )
}