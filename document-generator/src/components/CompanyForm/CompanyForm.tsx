import { Form, Input, Select, DatePicker } from "antd"
import { Wrapper, TwoColumns, SectionTitle } from "./styles"

const { Option } = Select

export const CompanyForm = () => {
  return (
    <Wrapper>
      <SectionTitle>Company Details</SectionTitle>

      <TwoColumns>
        <Form.Item label="Company Type" name="companyType">
          <Select>
            <Option value="UAB">UAB</Option>
            <Option value="MB">Small Partnership (MB)</Option>
            <Option value="II">Individual Enterprise (II)</Option>
            <Option value="AB">Joint Stock Company (AB)</Option>
            <Option value="OTHER">Other</Option>
          </Select>
        </Form.Item>

        <Form.Item label="Company Name" name="companyName">
          <Input />
        </Form.Item>

        <Form.Item label="Company Category" name="companyCategory">
          <Input />
        </Form.Item>

        <Form.Item label="Company Code" name="companyCode">
          <Input />
        </Form.Item>

        <Form.Item label="Address" name="address">
          <Input />
        </Form.Item>

        <Form.Item label="City / District" name="city">
          <Input />
        </Form.Item>

        <Form.Item label="Manager Position" name="managerType">
          <Select>
            <Option value="DIRECTOR">Director</Option>
            <Option value="CEO">CEO</Option>
            <Option value="MANAGER">Manager</Option>
            <Option value="OWNER">Owner</Option>
          </Select>
        </Form.Item>

        <Form.Item label="Manager Full Name" name="managerName">
          <Input />
        </Form.Item>

        <Form.Item label="Document Date" name="date">
          <DatePicker style={{ width: "100%" }} />
        </Form.Item>
      </TwoColumns>
    </Wrapper>
  )
}